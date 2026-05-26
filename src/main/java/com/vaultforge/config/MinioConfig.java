package com.vaultforge.config;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.MakeBucketArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
  @Bean
  public MinioClient minioClient(
      @Value("${vaultforge.minio.endpoint}") String endpoint,
      @Value("${vaultforge.minio.access-key}") String accessKey,
      @Value("${vaultforge.minio.secret-key}") String secretKey,
      @Value("${vaultforge.minio.bucket}") String bucket) {
    MinioClient client = MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build();
    try {
      boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
      if (!exists) {
        client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
      }
    } catch (Exception ex) {
      throw new IllegalStateException("MinIO bucket initialization failed", ex);
    }
    return client;
  }
}
