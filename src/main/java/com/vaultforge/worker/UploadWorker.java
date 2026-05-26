package com.vaultforge.worker;

import com.vaultforge.config.RabbitConfig;
import com.vaultforge.domain.UploadJob;
import com.vaultforge.event.UploadRequestedEvent;
import com.vaultforge.repository.UploadJobRepository;
import java.util.List;
import java.util.Map;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UploadWorker {
  private final UploadJobRepository uploadJobRepository;
  private final RabbitTemplate rabbitTemplate;
  private final int maxRetries;

  public UploadWorker(UploadJobRepository uploadJobRepository,
                      RabbitTemplate rabbitTemplate,
                      @Value("${vaultforge.rabbit.retry-max-attempts}") int maxRetries) {
    this.uploadJobRepository = uploadJobRepository;
    this.rabbitTemplate = rabbitTemplate;
    this.maxRetries = maxRetries;
  }

  @RabbitListener(queues = "vf.upload.queue")
  @Transactional
  public void handleUpload(UploadRequestedEvent event, Message message) {
    if (isRetryExhausted(message, RabbitConfig.UPLOAD_QUEUE)) {
      rabbitTemplate.convertAndSend(RabbitConfig.DLX_EXCHANGE, "upload.dlq", event);
      return;
    }
    UploadJob job = uploadJobRepository.findById(event.uploadJobId()).orElse(null);
    if (job == null) {
      return;
    }
    if (!"PENDING".equals(job.getStatus())) {
      return;
    }
    job.setStatus("PROCESSING");
    uploadJobRepository.save(job);
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
