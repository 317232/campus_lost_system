package com.campus.lostfound.websocket.config;

import com.campus.lostfound.common.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类
 * 配置 STOMP 协议的消息代理和端点
 *
 * @author CampusLostFound
 * @version 1.0
 * @since 2026-04-24
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final JwtUtils jwtUtils;

    public WebSocketConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 配置消息代理前缀
        // /topic 用于广播消息（如系统公告）
        // /queue 用于点对点消息（如用户私有通知）
        config.enableSimpleBroker("/topic", "/queue");
        // 配置应用目的地前缀，客户端发送消息时需要使用
        config.setApplicationDestinationPrefixes("/app");
        // 配置用户目的地前缀，用于点对点消息
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 STOMP 端点，客户端通过该端点建立 WebSocket 连接
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // 也支持原生 WebSocket 连接（无 SockJS 降级）
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
        
        log.info("WebSocket STOMP 端点已注册: /ws");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 从 header 中获取 token
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    
                    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        
                        try {
                            // 验证 token 并设置用户信息
                            Long userId = jwtUtils.getUserIdFromToken(token);
                            accessor.setUser(() -> String.valueOf(userId));
                        } catch (Exception e) {
                            log.warn("WebSocket token 验证失败: {}", e.getMessage());
                        }
                    }
                }
                
                return message;
            }
        });
    }
}