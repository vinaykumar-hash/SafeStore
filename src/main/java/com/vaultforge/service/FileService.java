package com.vaultforge.service;

import com.vaultforge.audit.Audit;
import com.vaultforge.domain.FileMetadata;
import com.vaultforge.domain.UploadJob;
import com.vaultforge.dto.file.UploadCompleteRequest;
import com.vaultforge.dto.file.UploadInitRequest;
import com.vaultforge.dto.file.UploadResponse;
import com.vaultforge.exception.NotFoundException;
import com.vaultforge.repository.ChunkRepository;
import com.vaultforge.repository.FileChunkMappingRepository;
import com.vaultforge.repository.FileMetadataRepository;
import com.vaultforge.repository.UploadJobRepository;
import com.vaultforge.repository.FileVersionRepository;
import com.vaultforge.storage.StorageService;
import com.vaultforge.util.HashingUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileService {
  private final FileMetadataRepository fileMetadataRepository;
  private final UploadJobRepository uploadJobRepository;
  private final UploadManager uploadManager;
  private final SnapshotService snapshotService;
  private final IntegrityService integrityService;
  private final long chunkSizeBytes;
  private final ConcurrencyService concurrencyService;
  private final FileChunkMappingRepository mappingRepository;
  private final ChunkRepository chunkRepository;
  private final ChunkLifecycleService chunkLifecycleService;
  private final FileVersionRepository fileVersionRepository;
  private final StorageService storageService;
  private final DeduplicationService deduplicationService;
  private final FileChunkMappingService mappingService;

  public FileService(FileMetadataRepository fileMetadataRepository,
                     UploadJobRepository uploadJobRepository,
                     UploadManager uploadManager,
                     SnapshotService snapshotService,
                     IntegrityService integrityService,
                     @Value("${vaultforge.storage.chunk-size-bytes}") long chunkSizeBytes,
                     ConcurrencyService concurrencyService,
                     FileChunkMappingRepository mappingRepository,
                     ChunkRepository chunkRepository,
                     ChunkLifecycleService chunkLifecycleService,
                     FileVersionRepository fileVersionRepository,
                     StorageService storageService,
                     DeduplicationService deduplicationService,
                     FileChunkMappingService mappingService) {
    this.fileMetadataRepository = fileMetadataRepository;
    this.uploadJobRepository = uploadJobRepository;
    this.uploadManager = uploadManager;
    this.snapshotService = snapshotService;
    this.integrityService = integrityService;
    this.chunkSizeBytes = chunkSizeBytes;
    this.concurrencyService = concurrencyService;
    this.mappingRepository = mappingRepository;
    this.chunkRepository = chunkRepository;
    this.chunkLifecycleService = chunkLifecycleService;
    this.fileVersionRepository = fileVersionRepository;
    this.storageService = storageService;
    this.deduplicationService = deduplicationService;
    this.mappingService = mappingService;
  }

  @Audit(action = "UPLOAD_INIT", entityType = "FILE")
  @CacheEvict(cacheNames = "metadata", allEntries = true)
  public UploadResponse initiateUpload(UUID userId, UploadInitRequest request) {
    FileMetadata metadata = new FileMetadata();
    metadata.setFilename(request.filename());
    metadata.setOwnerId(userId);
    metadata.setTotalSize(request.totalSize());
    metadata.setChecksum("PENDING");
    metadata.setTags(request.tags());
    fileMetadataRepository.save(metadata);

    UploadJob job = new UploadJob();
    job.setUserId(userId);
    job.setFileId(metadata.getId());
    job.setStatus("PENDING");
    uploadJobRepository.save(job);

    uploadManager.publishUpload(job.getId(), metadata.getId(), userId);
    return new UploadResponse(job.getId(), metadata.getId(), job.getStatus());
  }

  @Audit(action = "UPLOAD_COMPLETE", entityType = "FILE", entityIdArg = 0)
  @CacheEvict(cacheNames = "metadata", allEntries = true)
  public void completeUpload(UUID fileId, UploadCompleteRequest request) {
    concurrencyService.runWithRetry(() -> {
      FileMetadata metadata = fileMetadataRepository.findById(fileId)
          .orElseThrow(() -> new NotFoundException("File not found"));
      metadata.setChecksum(request.checksum());
      fileMetadataRepository.save(metadata);
      snapshotService.createSnapshot(fileId, request.checksum());
      integrityService.assertIntegrity(fileId, request.checksum());
    });
  }

  public UploadResponse uploadMultipart(UUID userId, MultipartFile file, String tags) {
    UploadInitRequest initRequest = new UploadInitRequest(file.getOriginalFilename(), file.getSize(), tags);
    UploadResponse response = initiateUpload(userId, initRequest);
    if (chunkSizeBytes > Integer.MAX_VALUE) {
      throw new IllegalStateException("Chunk size exceeds max buffer size");
    }
    int bufferSize = (int) chunkSizeBytes;
    byte[] buffer = new byte[bufferSize];
    int order = 0;
    MessageDigest fileDigest = HashingUtil.sha256Digest();
    try (InputStream inputStream = file.getInputStream()) {
      int read;
      while ((read = inputStream.read(buffer)) != -1) {
        byte[] chunkBytes = new byte[read];
        System.arraycopy(buffer, 0, chunkBytes, 0, read);
        fileDigest.update(chunkBytes);
        String hash = HashingUtil.sha256(chunkBytes);
        String storagePath = "chunks/" + hash;
        if (!deduplicationService.chunkExists(hash)) {
          storageService.putChunk(storagePath, new ByteArrayInputStream(chunkBytes), read);
        }
        var chunk = deduplicationService.ensureChunk(hash, storagePath, read);
        mappingService.addMapping(response.fileId(), chunk.getId(), order);
        order++;
      }
    } catch (IOException ex) {
      throw new IllegalStateException("Multipart upload failed", ex);
    }
    String checksum = HashingUtil.toHex(fileDigest.digest());
    FileMetadata metadata = fileMetadataRepository.findById(response.fileId())
        .orElseThrow(() -> new NotFoundException("File not found"));
    metadata.setChecksum(checksum);
    fileMetadataRepository.save(metadata);
    snapshotService.createSnapshot(response.fileId(), checksum);
    uploadJobRepository.findById(response.uploadJobId()).ifPresent(job -> {
      job.setStatus("COMPLETED");
      uploadJobRepository.save(job);
    });
    return new UploadResponse(response.uploadJobId(), response.fileId(), "COMPLETED");
  }

  public Optional<FileMetadata> getMetadata(UUID fileId) {
    return fileMetadataRepository.findById(fileId);
  }

  @Audit(action = "FILE_DELETE", entityType = "FILE", entityIdArg = 0)
  @CacheEvict(cacheNames = "metadata", allEntries = true)
  @Transactional
  public void deleteFile(UUID fileId) {
    FileMetadata metadata = fileMetadataRepository.findById(fileId)
        .orElseThrow(() -> new NotFoundException("File not found"));
    mappingRepository.findByFileIdOrderByChunkOrder(fileId).forEach(mapping -> {
      chunkRepository.findById(mapping.getChunkId()).ifPresent(chunk ->
          chunkLifecycleService.deleteByHash(chunk.getHash()));
    });
    mappingRepository.deleteByFileId(fileId);
    fileVersionRepository.deleteByFileId(fileId);
    uploadJobRepository.deleteByFileId(fileId);
    fileMetadataRepository.delete(metadata);
  }
}
