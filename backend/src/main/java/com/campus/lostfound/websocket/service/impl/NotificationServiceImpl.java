package com.campus.lostfound.websocket.service.impl;

import com.campus.lostfound.websocket.dto.NotificationMessage;
import com.campus.lostfound.websocket.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * WebSocket 实时通知服务实现
 *
 * @author CampusLostFound
 * @version 1.0
 * @since 2026-04-24
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendToUser(Long userId, NotificationMessage message) {
        // 使用 /queue 路径推送私有消息，Spring 会自动路由到对应用户
        String destination = "/queue/notify";
        messagingTemplate.convertAndSendToUser(
            String.valueOf(userId),
            destination,
            message
        );
        log.info("通知已发送至用户 userId={}, type={}", userId, message.getType());
    }

    @Override
    public void broadcast(NotificationMessage message) {
        // 使用 /topic 路径广播消息
        String destination = "/topic/public";
        messagingTemplate.convertAndSend(destination, message);
        log.info("广播通知已发送, type={}", message.getType());
    }

    @Override
    public void notifyClaimResult(Long claimId, Long userId, boolean approved, String reason) {
        NotificationMessage message = NotificationMessage.builder()
                .type(approved ? NotificationMessage.Type.CLAIM_APPROVED : NotificationMessage.Type.CLAIM_REJECTED)
                .title(approved ? "认领申请通过" : "认领申请被拒绝")
                .content(approved 
                        ? "您的认领申请（ID: " + claimId + "）已通过审核" 
                        : "您的认领申请（ID: " + claimId + "）被拒绝。原因：" + (reason != null ? reason : "无"))
                .businessId(claimId)
                .businessType(NotificationMessage.BusinessType.CLAIM)
                .sentAt(LocalDateTime.now())
                .toUserId(userId)
                .build();
        
        sendToUser(userId, message);
    }

    @Override
    public void notifyItemAuditResult(Long itemId, Long userId, boolean approved, String reason) {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationMessage.Type.ITEM_AUDITED)
                .title(approved ? "物品发布审核通过" : "物品发布审核被拒绝")
                .content(approved 
                        ? "您发布的物品（ID: " + itemId + "）已通过审核" 
                        : "您发布的物品（ID: " + itemId + "）被拒绝。原因：" + (reason != null ? reason : "无"))
                .businessId(itemId)
                .businessType(NotificationMessage.BusinessType.ITEM)
                .sentAt(LocalDateTime.now())
                .toUserId(userId)
                .build();
        
        sendToUser(userId, message);
    }
}