package com.vaultforge.controller;

import com.vaultforge.dto.file.ChunkUploadRequest;
import com.vaultforge.dto.file.UploadCompleteRequest;
import com.vaultforge.dto.file.UploadInitRequest;
import com.vaultforge.dto.file.UploadResponse;
import com.vaultforge.domain.FileMetadata;
import com.vaultforge.exception.NotFoundException;
import com.vaultforge.service.FileService;
import com.vaultforge.service.UploadManager;
import com.vaultforge.service.ObjectReconstructionService;
import com.vaultforge.service.MetadataService;
import com.vaultforge.repository.UploadJobRepository;
import com.vaultforge.repository.UserRepository;
import com.vaultforge.exception.BadRequestException;
import com.vaultforge.util.HashingUtil;
import jakarta.validation.Valid;
import java.util.Base64;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
  private final FileService fileService;
  private final UploadManager uploadManager;
  private final UploadJobRepository uploadJobRepository;
  private final UserRepository userRepository;
  private final ObjectReconstructionService reconstructionService;
  private final MetadataService metadataService;

  public FileController(FileService fileService,
                        UploadManager uploadManager,
                        UploadJobRepository uploadJobRepository,
                        UserRepository userRepository,
                        ObjectReconstructionService reconstructionService,
                        MetadataService metadataService) {
    this.fileService = fileService;
    this.uploadManager = uploadManager;
    this.uploadJobRepository = uploadJobRepository;
    this.userRepository = userRepository;
    this.reconstructionService = reconstructionService;
    this.metadataService = metadataService;
  }

  @PostMapping("/upload/initiate")
  public ResponseEntity<UploadResponse> initiate(@Valid @RequestBody UploadInitRequest request,
                                                 Authentication authentication) {
    UUID userId = resolveUserId(authentication);
    return ResponseEntity.ok(fileService.initiateUpload(userId, request));
  }

  @PostMapping(value = "/upload/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UploadResponse> uploadMultipart(@RequestParam("file") MultipartFile file,
                                                        @RequestParam(value = "tags", required = false) String tags,
                                                        Authentication authentication) {
    UUID userId = resolveUserId(authentication);
    return ResponseEntity.ok(fileService.uploadMultipart(userId, file, tags));
  }

  @PostMapping("/upload/{fileId}/chunk")
  public ResponseEntity<Void> uploadChunk(@PathVariable("fileId") UUID fileId,
                                          @Valid @RequestBody ChunkUploadRequest request) {
    byte[] data = Base64.getDecoder().decode(request.dataBase64());
    String computedHash = HashingUtil.sha256(data);
    if (!computedHash.equals(request.chunkHash())) {
      throw new BadRequestException("Chunk hash mismatch");
    }
    UUID uploadJobId = uploadJobRepository.findFirstByFileIdOrderByCreatedAtDesc(fileId)
        .map(job -> job.getId())
        .orElseThrow(() -> new BadRequestException("Upload job not found"));
    uploadManager.publishChunk(uploadJobId, fileId, request.chunkOrder(), request.chunkHash(),
        request.storagePath(), request.sizeBytes(), request.dataBase64());
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/upload/{fileId}/complete")
  public ResponseEntity<Void> complete(@PathVariable("fileId") UUID fileId,
                                       @Valid @RequestBody UploadCompleteRequest request) {
    fileService.completeUpload(fileId, request);
    return ResponseEntity.accepted().build();
  }

  @GetMapping("/{fileId}/download")
  public ResponseEntity<?> download(@PathVariable("fileId") UUID fileId) {
    FileMetadata metadata = fileService.getMetadata(fileId)
        .orElseThrow(() -> new NotFoundException("File not found"));
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFilename() + "\"")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(reconstructionService.streamFile(fileId));
  }

  @GetMapping("/{fileId}/metadata")
  public ResponseEntity<?> metadata(@PathVariable("fileId") UUID fileId) {
    return ResponseEntity.ok(metadataService.getMetadata(fileId)
        .orElseThrow(() -> new NotFoundException("File not found")));
  }

  @DeleteMapping("/{fileId}")
  public ResponseEntity<Void> delete(@PathVariable("fileId") UUID fileId) {
    fileService.deleteFile(fileId);
    return ResponseEntity.noContent().build();
  }

  private UUID resolveUserId(Authentication authentication) {
    return userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new NotFoundException("Authenticated user not found"))
        .getId();
  }
}
