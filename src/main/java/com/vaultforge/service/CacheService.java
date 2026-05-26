package com.vaultforge.service;

import java.util.Collection;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
  private final CacheManager cacheManager;

  public CacheService(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  public void evictAll(String cacheName) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.clear();
    }
  }

  public void evictKey(String cacheName, Object key) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.evict(key);
    }
  }

  public Collection<String> listCaches() {
    return cacheManager.getCacheNames();
  }
}
