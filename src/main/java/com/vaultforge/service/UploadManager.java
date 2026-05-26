package com.vaultforge.service;

import com.vaultforge.config.RabbitConfig;
import com.vaultforge.event.UploadRequestedEvent;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class UploadManager {
  private final RabbitTemplate rabbitTemplate;

  public UploadManager(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publishUpload(UUID uploadJobId, UUID fileId, UUID userId) {
    rabbitTemplate.convertAndSend(RabbitConfig.UPLOAD_EXCHANGE, "upload",
        new UploadRequestedEvent(uploadJobId, fileId, userId));
  }

  public void publishChunk(UUID uploadJobId,
                           UUID fileId,
                           int chunkOrder,
                           String chunkHash,
                           String storagePath,
                           long sizeBytes,
                           String dataBase64) {
    rabbitTemplate.convertAndSend(RabbitConfig.UPLOAD_EXCHANGE, "chunk",
        new com.vaultforge.event.ChunkUploadEvent(uploadJobId, fileId, chunkOrder,
            chunkHash, storagePath, sizeBytes, dataBase64));
  }
}
