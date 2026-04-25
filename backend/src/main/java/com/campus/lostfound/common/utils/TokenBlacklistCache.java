package com.campus.lostfound.common.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token 黑名单缓存
 * 注意：当前使用本地内存缓存，不支持集群部署。
 * 如果部署多节点应用，需要更换为 Redis 等分布式缓存。
 */
@Component
public class TokenBlacklistCache {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void add(String token, long expireTimeMillis) {
        blacklist.put(token, expireTimeMillis);
    }

    public boolean isBlacklisted(String token) {
        Long expireTime = blacklist.get(token);
        if (expireTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > expireTime) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}
