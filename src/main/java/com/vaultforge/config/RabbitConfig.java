package com.vaultforge.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {
  public static final String UPLOAD_EXCHANGE = "vf.upload.exchange";
  public static final String RETRY_EXCHANGE = "vf.retry.exchange";
  public static final String DLX_EXCHANGE = "vf.dlq.exchange";
  public static final String UPLOAD_QUEUE = "vf.upload.queue";
  public static final String UPLOAD_RETRY_QUEUE = "vf.upload.retry";
  public static final String UPLOAD_DLQ = "vf.upload.dlq";
  public static final String CHUNK_QUEUE = "vf.chunk.queue";
  public static final String CHUNK_RETRY_QUEUE = "vf.chunk.retry";
  public static final String CHUNK_DLQ = "vf.chunk.dlq";

  @Bean
  public DirectExchange uploadExchange() {
    return new DirectExchange(UPLOAD_EXCHANGE);
  }

  @Bean
  public DirectExchange retryExchange() {
    return new DirectExchange(RETRY_EXCHANGE);
  }

  @Bean
  public DirectExchange deadLetterExchange() {
    return new DirectExchange(DLX_EXCHANGE);
  }

  @Bean
  public Queue uploadQueue() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-dead-letter-exchange", RETRY_EXCHANGE);
    args.put("x-dead-letter-routing-key", "upload.retry");
    return new Queue(UPLOAD_QUEUE, true, false, false, args);
  }

  @Bean
  public Queue uploadRetryQueue() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-message-ttl", 15000);
    args.put("x-dead-letter-exchange", UPLOAD_EXCHANGE);
    args.put("x-dead-letter-routing-key", "upload");
    return new Queue(UPLOAD_RETRY_QUEUE, true, false, false, args);
  }

  @Bean
  public Queue uploadDlq() {
    return new Queue(UPLOAD_DLQ, true, false, false);
  }

  @Bean
  public Queue chunkQueue() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-dead-letter-exchange", RETRY_EXCHANGE);
    args.put("x-dead-letter-routing-key", "chunk.retry");
    return new Queue(CHUNK_QUEUE, true, false, false, args);
  }

  @Bean
  public Queue chunkRetryQueue() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-message-ttl", 15000);
    args.put("x-dead-letter-exchange", UPLOAD_EXCHANGE);
    args.put("x-dead-letter-routing-key", "chunk");
    return new Queue(CHUNK_RETRY_QUEUE, true, false, false, args);
  }

  @Bean
  public Queue chunkDlq() {
    return new Queue(CHUNK_DLQ, true, false, false);
  }

  @Bean
  public Binding uploadBinding(
      @Qualifier("uploadExchange") DirectExchange uploadExchange,
      @Qualifier("uploadQueue") Queue uploadQueue) {
    return BindingBuilder.bind(uploadQueue).to(uploadExchange).with("upload");
  }

  @Bean
  public Binding uploadRetryBinding(
      @Qualifier("retryExchange") DirectExchange retryExchange,
      @Qualifier("uploadRetryQueue") Queue uploadRetryQueue) {
    return BindingBuilder.bind(uploadRetryQueue).to(retryExchange).with("upload.retry");
  }

  @Bean
  public Binding uploadDlqBinding(
      @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange,
      @Qualifier("uploadDlq") Queue uploadDlq) {
    return BindingBuilder.bind(uploadDlq).to(deadLetterExchange).with("upload.dlq");
  }

  @Bean
  public Binding chunkBinding(
      @Qualifier("uploadExchange") DirectExchange uploadExchange,
      @Qualifier("chunkQueue") Queue chunkQueue) {
    return BindingBuilder.bind(chunkQueue).to(uploadExchange).with("chunk");
  }

  @Bean
  public Binding chunkRetryBinding(
      @Qualifier("retryExchange") DirectExchange retryExchange,
      @Qualifier("chunkRetryQueue") Queue chunkRetryQueue) {
    return BindingBuilder.bind(chunkRetryQueue).to(retryExchange).with("chunk.retry");
  }

  @Bean
  public Binding chunkDlqBinding(
      @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange,
      @Qualifier("chunkDlq") Queue chunkDlq) {
    return BindingBuilder.bind(chunkDlq).to(deadLetterExchange).with("chunk.dlq");
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      Jackson2JsonMessageConverter messageConverter) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(messageConverter);
    factory.setDefaultRequeueRejected(false);
    factory.setPrefetchCount(16);
    return factory;
  }
}
