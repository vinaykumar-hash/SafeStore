package com.vaultforge.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableCaching
public class RedisConfig {
  @Value("${vaultforge.cache.metadata-ttl-seconds:300}")
  private long metadataTtlSeconds;

  @Value("${vaultforge.cache.snapshot-ttl-seconds:600}")
  private long snapshotTtlSeconds;

  @Value("${vaultforge.cache.chunk-exists-ttl-seconds:300}")
  private long chunkExistsTtlSeconds;

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10));
    Map<String, RedisCacheConfiguration> configs = new HashMap<>();
    configs.put("metadata", defaultConfig.entryTtl(Duration.ofSeconds(metadataTtlSeconds)));
    configs.put("snapshot-summaries", defaultConfig.entryTtl(Duration.ofSeconds(snapshotTtlSeconds)));
    configs.put("chunk-exists", defaultConfig.entryTtl(Duration.ofSeconds(chunkExistsTtlSeconds)));
    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(configs)
        .build();
  }

  @Bean
  public SimpleKeyGenerator cacheKeyGenerator() {
    return new SimpleKeyGenerator();
  }
}
