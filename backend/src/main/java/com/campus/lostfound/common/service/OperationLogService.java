package com.campus.lostfound.common.service;

import com.campus.lostfound.domain.entity.OperationLog;

/**
 * 操作日志服务
 * 用于记录系统关键操作的审计日志
 */
public interface OperationLogService {

    /**
     * 记录操作日志
     * @param targetType 操作对象类型（ITEM, CLAIM, USER, NOTICE 等）
     * @param targetId 操作对象ID
     * @param action 操作动作（CREATE, UPDATE, DELETE, APPROVE, REJECT 等）
     * @param fromStatus 原状态（可为null）
     * @param toStatus 目标状态（可为null）
     * @param remark 备注信息
     */
    void log(String targetType, Long targetId, String action, String fromStatus, String toStatus, String remark);

    /**
     * 记录操作日志（简化版）
     */
    default void log(String targetType, Long targetId, String action) {
        log(targetType, targetId, action, null, null, null);
    }
}