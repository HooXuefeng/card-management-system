package com.example.card.util;

import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class RedisUtil {
    // 使用内存缓存替代Redis
    private final Map<String, CacheItem> cache = new ConcurrentHashMap<>();
    
    private static class CacheItem {
        Object value;
        long expireTime;
        
        CacheItem(Object value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
    }

    public void setCache(String key, Object value, long expire) {
        long expireTime = System.currentTimeMillis() + (expire * 1000);
        cache.put(key, new CacheItem(value, expireTime));
    }

    public Object getCache(String key) {
        CacheItem item = cache.get(key);
        if (item == null) {
            return null;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > item.expireTime) {
            cache.remove(key);
            return null;
        }
        
        return item.value;
    }

    public void deleteCache(String key) {
        cache.remove(key);
    }

    public String tryLock(String lockKey, long expire) {
        String requestId = UUID.randomUUID().toString();
        long expireTime = System.currentTimeMillis() + (expire * 1000);
        
        synchronized (this) {
            CacheItem existingItem = cache.get(lockKey);
            
            // 检查锁是否存在且未过期
            if (existingItem != null && System.currentTimeMillis() <= existingItem.expireTime) {
                return null; // 锁已被占用
            }
            
            // 设置新锁
            cache.put(lockKey, new CacheItem(requestId, expireTime));
            return requestId;
        }
    }

    public boolean releaseLock(String lockKey, String requestId) {
        synchronized (this) {
            CacheItem item = cache.get(lockKey);
            
            // 检查锁是否存在且属于当前请求
            if (item != null && item.value.equals(requestId)) {
                cache.remove(lockKey);
                return true;
            }
            
            return false;
        }
    }
}