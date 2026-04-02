package com.shuaiqi.notification.websocket;

import com.shuaiqi.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;

import jakarta.websocket.*;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/ws/notification/{userId}", configurator = WebSocketServer.TokenConfigurator.class)
public class WebSocketServer {

    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    public static class TokenConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        public boolean modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            Map<String, List<String>> headers = request.getParameterMap();
            String userId = sec.getPathParameters().get("userId");

            List<String> tokenHeaders = request.getHeaders().get("Sec-WebSocket-Protocol");
            if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
                String token = tokenHeaders.get(0);
                if (token != null && !token.isEmpty() && JwtUtils.validateToken(token)) {
                    String tokenUserId = JwtUtils.getUserId(token);
                    if (!userId.equals(tokenUserId)) {
                        log.warn("WebSocket 认证失败: token用户ID {} 与路径参数 {} 不匹配", tokenUserId, userId);
                        return false;
                    }
                    return true;
                }
            }

            log.warn("WebSocket 连接缺少有效认证: userId={}", userId);
            return false;
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        Session oldSession = SESSION_MAP.put(userId, session);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                oldSession.close();
            } catch (IOException e) {
                log.warn("关闭用户 {} 的旧 WebSocket 连接失败: {}", userId, e.getMessage());
            }
        }
        log.info("用户 {} WebSocket 连接成功，当前在线人数: {}", userId, SESSION_MAP.size());
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        SESSION_MAP.remove(userId);
        log.info("用户 {} WebSocket 连接关闭，当前在线人数: {}", userId, SESSION_MAP.size());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        log.info("收到用户 {} 的消息: {}", userId, message);
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") String userId) {
        log.error("用户 {} WebSocket 发生错误: {}", userId, error.getMessage());
        SESSION_MAP.remove(userId);
    }

    public static void sendMessage(String userId, String message) {
        Session session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败: {}", userId, e.getMessage());
            }
        }
    }

    public static void broadcast(String message) {
        SESSION_MAP.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
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
        Session session = SESSION_MAP.get(userId);
        return session != null && session.isOpen();
    }
}
