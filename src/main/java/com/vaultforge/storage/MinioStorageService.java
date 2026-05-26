package com.vaultforge.storage;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MinioStorageService implements StorageService {
  private final MinioClient minioClient;
  private final String bucket;

  public MinioStorageService(MinioClient minioClient, @Value("${vaultforge.minio.bucket}") String bucket) {
    this.minioClient = minioClient;
    this.bucket = bucket;
  }

  @Override
  public void putChunk(String objectKey, InputStream data, long size) {
    try {
      minioClient.putObject(PutObjectArgs.builder()
          .bucket(bucket)
          .object(objectKey)
          .stream(data, size, -1)
          .build());
    } catch (Exception e) {
      throw new IllegalStateException("MinIO upload failed", e);
    }
  }

  @Override
  public InputStream getChunk(String objectKey) {
    try {
      return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      throw new IllegalStateException("MinIO download failed", e);
    }
  }

  @Override
  public void deleteChunk(String objectKey) {
    try {
      minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      throw new IllegalStateException("MinIO delete failed", e);
    }
  }
}
