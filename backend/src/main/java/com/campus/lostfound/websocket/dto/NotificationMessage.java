package com.campus.lostfound.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket 通知消息 DTO
 * 用于通过 WebSocket 推送的通知消息结构
 *
 * @author CampusLostFound
 * @version 1.0
 * @since 2026-04-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    /**
     * 通知类型
     */
    private String type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联业务ID（如物品ID、认领ID等）
     */
    private Long businessId;

    /**
     * 关联业务类型（如 ITEM、CLAIM 等）
     */
    private String businessType;

    /**
     * 发送时间
     */
    private LocalDateTime sentAt;

    /**
     * 发送人ID
     */
    private Long fromUserId;

    /**
     * 目标用户ID（用于点对点通知）
     */
    private Long toUserId;

    /**
     * 通知类型常量
     */
    public static class Type {
        public static final String CLAIM_APPROVED = "CLAIM_APPROVED";
        public static final String CLAIM_REJECTED = "CLAIM_REJECTED";
        public static final String ITEM_AUDITED = "ITEM_AUDITED";
        public static final String SYSTEM_NOTICE = "SYSTEM_NOTICE";
    }

    /**
     * 业务类型常量
     */
    public static class BusinessType {
        public static final String ITEM = "ITEM";
        public static final String CLAIM = "CLAIM";
        public static final String REPORT = "REPORT";
    }
}