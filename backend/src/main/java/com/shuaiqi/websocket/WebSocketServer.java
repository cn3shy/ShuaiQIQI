package com.shuaiqi.websocket;

import com.shuaiqi.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring WebSocket 实现的通知服务
 * 使用 Spring WebSocket API 替代 Jakarta WebSocket API
 */
@Slf4j
@Component
public class WebSocketServer extends TextWebSocketHandler {

    private static final ConcurrentHashMap<String, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session);
        if (token == null || !JwtUtils.validateToken(token)) {
            log.warn("WebSocket 连接缺少有效认证");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        String userId = JwtUtils.getUserId(token);
        session.getAttributes().put("userId", userId);

        WebSocketSession oldSession = SESSION_MAP.put(userId, session);
        if (oldSession != null && oldSession.isOpen()) {
            oldSession.close();
        }
        log.info("用户 {} WebSocket 连接成功，当前在线人数: {}", userId, SESSION_MAP.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = getUserId(session);
        log.info("收到用户 {} 的消息: {}", userId, message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable error) throws Exception {
        String userId = getUserId(session);
        log.error("用户 {} WebSocket 发生错误: {}", userId, error.getMessage());
        if (userId != null) {
            SESSION_MAP.remove(userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserId(session);
        if (userId != null) {
            SESSION_MAP.remove(userId);
            log.info("用户 {} WebSocket 连接关闭，当前在线人数: {}", userId, SESSION_MAP.size());
        }
    }

    public static void sendMessage(String userId, String message) {
        WebSocketSession session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败: {}", userId, e.getMessage());
            }
        }
    }

    public static void broadcast(String message) {
        SESSION_MAP.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("群发消息给用户 {} 失败: {}", userId, e.getMessage());
                }
            }
        });
    }

    public static int getOnlineCount() {
        return SESSION_MAP.size();
    }

    public static boolean isOnline(String userId) {
        WebSocketSession session = SESSION_MAP.get(userId);
        return session != null && session.isOpen();
    }

    private String getUserId(WebSocketSession session) {
        if (session == null) return null;
        Map<String, Object> attrs = session.getAttributes();
        return attrs != null ? (String) attrs.get("userId") : null;
    }

    /**
     * 从 WebSocket Session 中提取 token
     * 支持从 query string 和 header 中获取
     */
    private String extractToken(WebSocketSession session) {
        // 优先从 query string 获取 token
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }
        // 从 header 获取 Sec-WebSocket-Protocol
        Map<String, Object> headers = session.getAttributes();
        if (headers != null && headers.containsKey("handshakeHeaders")) {
            @SuppressWarnings("unchecked")
            Map<String, List<String>> handshakeHeaders =
                (Map<String, List<String>>) headers.get("handshakeHeaders");
            if (handshakeHeaders != null) {
                List<String> protocols = handshakeHeaders.get("Sec-WebSocket-Protocol");
                if (protocols != null && !protocols.isEmpty()) {
                    return protocols.get(0);
                }
            }
        }
        return null;
    }
}
