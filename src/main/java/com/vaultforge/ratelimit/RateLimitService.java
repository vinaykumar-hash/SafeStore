package com.vaultforge.ratelimit;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
  private final StringRedisTemplate redisTemplate;
  private final long requestsPerMinute;

  public RateLimitService(StringRedisTemplate redisTemplate,
                          @Value("${vaultforge.rate-limit.requests-per-minute}") long requestsPerMinute) {
    this.redisTemplate = redisTemplate;
    this.requestsPerMinute = requestsPerMinute;
  }

  public boolean tryConsume(String key) {
    Long count = redisTemplate.opsForValue().increment(key);
    if (count != null && count == 1L) {
      redisTemplate.expire(key, Duration.ofMinutes(1));
    }
    return count != null && count <= requestsPerMinute;
  }
}
