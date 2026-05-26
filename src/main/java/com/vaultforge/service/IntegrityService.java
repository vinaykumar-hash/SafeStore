package com.vaultforge.service;

import com.vaultforge.dto.integrity.CorruptChunkInfo;
import com.vaultforge.dto.integrity.IntegrityReportResponse;
import com.vaultforge.dto.integrity.IntegrityResponse;
import com.vaultforge.exception.ConflictException;
import com.vaultforge.exception.NotFoundException;
import com.vaultforge.repository.ChunkRepository;
import com.vaultforge.repository.FileChunkMappingRepository;
import com.vaultforge.repository.FileMetadataRepository;
import com.vaultforge.storage.StorageService;
import com.vaultforge.util.HashingUtil;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class IntegrityService {
  private final FileMetadataRepository fileMetadataRepository;
  private final FileChunkMappingRepository mappingRepository;
  private final ChunkRepository chunkRepository;
  private final StorageService storageService;

  public IntegrityService(FileMetadataRepository fileMetadataRepository,
                          FileChunkMappingRepository mappingRepository,
                          ChunkRepository chunkRepository,
                          StorageService storageService) {
    this.fileMetadataRepository = fileMetadataRepository;
    this.mappingRepository = mappingRepository;
    this.chunkRepository = chunkRepository;
    this.storageService = storageService;
  }

  public IntegrityResponse validate(UUID fileId) {
    return fileMetadataRepository.findById(fileId)
        .map(file -> {
          IntegrityResult result = computeIntegrity(fileId);
          boolean checksumMatches = file.getChecksum().equals(result.checksum());
          boolean valid = checksumMatches && result.corruptChunks().isEmpty();
          return new IntegrityResponse(file.getId(), valid, result.checksum());
        })
        .orElse(new IntegrityResponse(fileId, false, "UNKNOWN"));
  }

  public IntegrityReportResponse report(UUID fileId) {
    return fileMetadataRepository.findById(fileId)
        .map(file -> {
          IntegrityResult result = computeIntegrity(fileId);
          boolean checksumMatches = file.getChecksum().equals(result.checksum());
          boolean valid = checksumMatches && result.corruptChunks().isEmpty();
          return new IntegrityReportResponse(file.getId(), valid, result.checksum(), result.corruptChunks());
        })
        .orElse(new IntegrityReportResponse(fileId, false, "UNKNOWN", List.of()));
  }

  public void assertIntegrity(UUID fileId, String expectedChecksum) {
    String computed = computeIntegrity(fileId).checksum();
    if (!expectedChecksum.equals(computed)) {
      throw new ConflictException("Integrity check failed");
    }
  }

  private IntegrityResult computeIntegrity(UUID fileId) {
    MessageDigest fileDigest = HashingUtil.sha256Digest();
    List<CorruptChunkInfo> corrupt = new ArrayList<>();
    mappingRepository.findByFileIdOrderByChunkOrder(fileId).stream()
        .sorted(Comparator.comparingInt(mapping -> mapping.getChunkOrder()))
        .forEach(mapping -> {
          var chunk = chunkRepository.findById(mapping.getChunkId())
              .orElseThrow(() -> new NotFoundException("Chunk not found"));
          MessageDigest chunkDigest = HashingUtil.sha256Digest();
          try (InputStream input = storageService.getChunk(chunk.getStoragePath())) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
              fileDigest.update(buffer, 0, read);
              chunkDigest.update(buffer, 0, read);
            }
          } catch (Exception ex) {
            throw new IllegalStateException("Integrity read failed", ex);
          }
          String actualChunkHash = HashingUtil.toHex(chunkDigest.digest());
          if (!chunk.getHash().equals(actualChunkHash)) {
            corrupt.add(new CorruptChunkInfo(
                mapping.getChunkOrder(),
                chunk.getHash(),
                actualChunkHash,
                chunk.getStoragePath()
            ));
          }
        });
    return new IntegrityResult(HashingUtil.toHex(fileDigest.digest()), corrupt);
  }

  private record IntegrityResult(String checksum, List<CorruptChunkInfo> corruptChunks) {}
}
