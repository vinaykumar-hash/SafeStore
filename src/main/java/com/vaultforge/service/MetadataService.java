package com.vaultforge.service;

import com.vaultforge.dto.metadata.MetadataResponse;
import com.vaultforge.dto.metadata.MetadataSearchRequest;
import com.vaultforge.metadata.MetadataSpecifications;
import com.vaultforge.repository.FileMetadataRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MetadataService {
  private final FileMetadataRepository fileMetadataRepository;

  public MetadataService(FileMetadataRepository fileMetadataRepository) {
    this.fileMetadataRepository = fileMetadataRepository;
  }

  @Cacheable(cacheNames = "metadata")
  public List<MetadataResponse> listAll() {
    return fileMetadataRepository.findAll().stream()
        .map(file -> new MetadataResponse(
            file.getId(), file.getFilename(), file.getOwnerId(), file.getTotalSize(),
            file.getChecksum(), file.getUploadTimestamp(), file.getTags()))
        .collect(Collectors.toList());
  }

  public Page<MetadataResponse> search(MetadataSearchRequest request, Pageable pageable) {
    return fileMetadataRepository.findAll(MetadataSpecifications.from(request), pageable)
        .map(file -> new MetadataResponse(
            file.getId(), file.getFilename(), file.getOwnerId(), file.getTotalSize(),
            file.getChecksum(), file.getUploadTimestamp(), file.getTags()));
  }

  public Optional<MetadataResponse> getMetadata(UUID fileId) {
    return fileMetadataRepository.findById(fileId)
        .map(file -> new MetadataResponse(
            file.getId(), file.getFilename(), file.getOwnerId(), file.getTotalSize(),
            file.getChecksum(), file.getUploadTimestamp(), file.getTags()));
  }
}
