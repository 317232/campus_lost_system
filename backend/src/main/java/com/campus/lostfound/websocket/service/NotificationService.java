package com.campus.lostfound.websocket.service;

import com.campus.lostfound.websocket.dto.NotificationMessage;

/**
 * WebSocket 实时通知服务接口
 * 提供向用户推送实时通知的能力
 *
 * @author CampusLostFound
 * @version 1.0
 * @since 2026-04-24
 */
public interface NotificationService {

    /**
     * 向指定用户发送私有通知
     *
     * @param userId 目标用户ID
     * @param message 通知消息
     */
    void sendToUser(Long userId, NotificationMessage message);

    /**
     * 向所有在线用户发送广播通知
     *
     * @param message 通知消息
     */
    void broadcast(NotificationMessage message);

    /**
     * 发送认领审核结果通知
     *
     * @param claimId 认领申请ID
     * @param userId 目标用户ID
     * @param approved 是否通过
     * @param reason 原因（拒绝时填写）
     */
    void notifyClaimResult(Long claimId, Long userId, boolean approved, String reason);

    /**
     * 发送物品审核结果通知
     *
     * @param itemId 物品ID
     * @param userId 目标用户ID
     * @param approved 是否通过
     * @param reason 原因（拒绝时填写）
     */
    void notifyItemAuditResult(Long itemId, Long userId, boolean approved, String reason);
}