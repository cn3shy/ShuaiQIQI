package com.shuaiqi.notification.websocket;

import lombok.extern.slf4j.Slf4j;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端
 */
@Slf4j
@ServerEndpoint("/ws/notification/{userId}")
public class WebSocketServer {

    /**
     * 存放每个用户对应的 WebSocket 连接
     */
    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        SESSION_MAP.put(userId, session);
        log.info("用户 {} WebSocket 连接成功，当前在线人数: {}", userId, SESSION_MAP.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        SESSION_MAP.remove(userId);
        log.info("用户 {} WebSocket 连接关闭，当前在线人数: {}", userId, SESSION_MAP.size());
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        log.info("收到用户 {} 的消息: {}", userId, message);
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") String userId) {
        log.error("用户 {} WebSocket 发生错误: {}", userId, error.getMessage());
        SESSION_MAP.remove(userId);
    }

    /**
     * 发送消息给指定用户
     */
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

    /**
     * 群发消息
     */
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

    /**
     * 获取在线用户数
     */
    public static int getOnlineCount() {
        return SESSION_MAP.size();
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isOnline(String userId) {
        Session session = SESSION_MAP.get(userId);
        return session != null && session.isOpen();
    }
}
