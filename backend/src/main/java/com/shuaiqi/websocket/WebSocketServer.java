package com.shuaiqi.websocket;

import com.shuaiqi.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;

import jakarta.websocket.*;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/ws/notification", configurator = WebSocketServer.TokenConfigurator.class)
public class WebSocketServer {

    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    public static class TokenConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        public boolean modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            List<String> tokenHeaders = request.getHeaders().get("Sec-WebSocket-Protocol");
            if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
                String token = tokenHeaders.get(0);
                if (token != null && !token.isEmpty() && JwtUtils.validateToken(token)) {
                    String userId = JwtUtils.getUserId(token);
                    sec.getUserProperties().put("userId", userId);
                    return true;
                }
            }
            log.warn("WebSocket 连接缺少有效认证");
            return false;
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String userId = (String) config.getUserProperties().get("userId");
        if (userId == null) {
            try { session.close(); } catch (IOException e) {
                log.warn("关闭未认证 WebSocket 连接失败: {}", e.getMessage());
            }
            return;
        }
        Session oldSession = SESSION_MAP.put(userId, session);
        if (oldSession != null && oldSession.isOpen()) {
            try { oldSession.close(); } catch (IOException e) {
                log.warn("关闭用户 {} 的旧 WebSocket 连接失败: {}", userId, e.getMessage());
            }
        }
        log.info("用户 {} WebSocket 连接成功，当前在线人数: {}", userId, SESSION_MAP.size());
    }

    @OnClose
    public void onClose(Session session) {
        String userId = getUserId(session);
        if (userId != null) {
            SESSION_MAP.remove(userId);
            log.info("用户 {} WebSocket 连接关闭，当前在线人数: {}", userId, SESSION_MAP.size());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到用户 {} 的消息: {}", getUserId(session), message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        String userId = getUserId(session);
        log.error("用户 {} WebSocket 发生错误: {}", userId, error.getMessage());
        if (userId != null) SESSION_MAP.remove(userId);
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
                try { session.getBasicRemote().sendText(message); }
                catch (IOException e) { log.error("群发消息给用户 {} 失败: {}", userId, e.getMessage()); }
            }
        });
    }

    public static int getOnlineCount() { return SESSION_MAP.size(); }

    public static boolean isOnline(String userId) {
        Session session = SESSION_MAP.get(userId);
        return session != null && session.isOpen();
    }

    private String getUserId(Session session) {
        return (String) session.getUserProperties().get("userId");
    }
}
