import { useState, useEffect, useCallback, useRef } from 'react';
import { message } from 'antd';
import type { Notification } from '@types';

interface UseWebSocketOptions {
  userId?: string;
  onNotification?: (notification: Notification) => void;
  reconnectInterval?: number;
  maxReconnectAttempts?: number;
}

const MAX_RECONNECT_ATTEMPTS = 10;
const BASE_RECONNECT_INTERVAL = 3000;
const MAX_RECONNECT_INTERVAL = 60000;

export const useWebSocket = (options: UseWebSocketOptions = {}) => {
  const { userId, onNotification, reconnectInterval = BASE_RECONNECT_INTERVAL, maxReconnectAttempts = MAX_RECONNECT_ATTEMPTS } = options;
  const [isConnected, setIsConnected] = useState(false);
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimerRef = useRef<NodeJS.Timeout | null>(null);
  const isUnmountedRef = useRef(false);
  const reconnectAttemptsRef = useRef(0);

  const getReconnectDelay = useCallback((attempt: number) => {
    return Math.min(BASE_RECONNECT_INTERVAL * Math.pow(2, attempt), MAX_RECONNECT_INTERVAL);
  }, []);

  const connect = useCallback(() => {
    if (!userId) return;
    const token = localStorage.getItem('token');
    if (!token) return;
    if (wsRef.current) wsRef.current.close();

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/notification`;

    try {
      const ws = new WebSocket(wsUrl, [token]);
      ws.onopen = () => {
        reconnectAttemptsRef.current = 0;
        if (!isUnmountedRef.current) setIsConnected(true);
      };
      ws.onmessage = (event) => {
        try {
          const notification: Notification = JSON.parse(event.data);
          if (!isUnmountedRef.current) {
            onNotification?.(notification);
            message.info(notification.title);
          }
        } catch (error) {
          console.error('解析通知消息失败:', error);
        }
      };
      ws.onclose = () => {
        if (!isUnmountedRef.current) setIsConnected(false);
        if (reconnectTimerRef.current) clearTimeout(reconnectTimerRef.current);
        const attempts = reconnectAttemptsRef.current;
        if (attempts < maxReconnectAttempts && !isUnmountedRef.current) {
          reconnectAttemptsRef.current = attempts + 1;
          const delay = getReconnectDelay(attempts);
          reconnectTimerRef.current = setTimeout(() => connect(), delay);
        }
      };
      ws.onerror = () => { if (!isUnmountedRef.current) setIsConnected(false); };
      wsRef.current = ws;
    } catch (error) {
      console.error('WebSocket 连接失败:', error);
    }
  }, [userId, onNotification, maxReconnectAttempts, getReconnectDelay]);

  const disconnect = useCallback(() => {
    if (reconnectTimerRef.current) { clearTimeout(reconnectTimerRef.current); reconnectTimerRef.current = null; }
    if (wsRef.current) { wsRef.current.close(); wsRef.current = null; }
    if (!isUnmountedRef.current) setIsConnected(false);
    reconnectAttemptsRef.current = 0;
  }, []);

  const send = useCallback((data: string) => {
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) wsRef.current.send(data);
  }, []);

  useEffect(() => {
    isUnmountedRef.current = false;
    if (userId) connect();
    return () => { isUnmountedRef.current = true; disconnect(); };
  }, [userId, connect, disconnect]);

  return { isConnected, connect, disconnect, send };
};
