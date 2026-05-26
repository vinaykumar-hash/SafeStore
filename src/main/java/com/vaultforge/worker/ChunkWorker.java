package com.vaultforge.worker;

import com.vaultforge.config.RabbitConfig;
import com.vaultforge.event.ChunkUploadEvent;
import com.vaultforge.service.DeduplicationService;
import com.vaultforge.service.FileChunkMappingService;
import com.vaultforge.storage.StorageService;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ChunkWorker {
  private final StorageService storageService;
  private final DeduplicationService deduplicationService;
  private final FileChunkMappingService mappingService;
  private final RabbitTemplate rabbitTemplate;
  private final int maxRetries;

  public ChunkWorker(StorageService storageService,
                     DeduplicationService deduplicationService,
                     FileChunkMappingService mappingService,
                     RabbitTemplate rabbitTemplate,
                     @Value("${vaultforge.rabbit.retry-max-attempts}") int maxRetries) {
    this.storageService = storageService;
    this.deduplicationService = deduplicationService;
    this.mappingService = mappingService;
    this.rabbitTemplate = rabbitTemplate;
    this.maxRetries = maxRetries;
  }

  @RabbitListener(queues = "vf.chunk.queue")
  @Transactional
  public void handleChunk(ChunkUploadEvent event, Message message) {
    if (isRetryExhausted(message, RabbitConfig.CHUNK_QUEUE)) {
      rabbitTemplate.convertAndSend(RabbitConfig.DLX_EXCHANGE, "chunk.dlq", event);
      return;
    }
    byte[] data = Base64.getDecoder().decode(event.dataBase64());
    boolean exists = deduplicationService.chunkExists(event.chunkHash());
    if (!exists) {
      storageService.putChunk(event.storagePath(), new ByteArrayInputStream(data), event.sizeBytes());
    }
    var chunk = deduplicationService.ensureChunk(event.chunkHash(), event.storagePath(), event.sizeBytes());
    mappingService.addMapping(event.fileId(), chunk.getId(), event.chunkOrder());
  }

  @SuppressWarnings("unchecked")
  private boolean isRetryExhausted(Message message, String queueName) {
    List<Map<String, Object>> deaths = (List<Map<String, Object>>) message.getMessageProperties()
        .getHeaders().get("x-death");
    if (deaths == null) {
      return false;
    }
    for (Map<String, Object> death : deaths) {
      Object queue = death.get("queue");
      if (queueName.equals(queue)) {
        Object count = death.get("count");
        if (count instanceof Long && ((Long) count) >= maxRetries) {
          return true;
        }
      }
    }
    return false;
  }
}
