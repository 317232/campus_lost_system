package com.campus.lostfound.common.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码缓存
 * 注意：当前使用本地内存缓存，不支持集群部署。
 * 如果部署多节点应用，需要更换为 Redis 等分布式缓存。
 */
@Component
public class VerifyCodeCache {

    private final Map<String, CacheItem> cache = new ConcurrentHashMap<>();

    public void put(String email, String code, long timeoutMillis) {
        cache.put(email, new CacheItem(code, System.currentTimeMillis() + timeoutMillis));
    }

    public String get(String email) {
        CacheItem item = cache.get(email);
        if (item == null) {
            return null;
        }
        if (System.currentTimeMillis() > item.expireTime) {
            cache.remove(email);
            return null;
        }
        return item.code;
    }

    public void remove(String email) {
        cache.remove(email);
    }

    private static class CacheItem {
        String code;
        long expireTime;

        CacheItem(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }
}
