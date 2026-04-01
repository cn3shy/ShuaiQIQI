// WebSocket Hook - 实时通知
import { useState, useEffect, useCallback, useRef } from 'react';
import { message } from 'antd';
import type { Notification } from '@types';

interface UseWebSocketOptions {
  userId?: string;
  onNotification?: (notification: Notification) => void;
  reconnectInterval?: number;
}

export const useWebSocket = (options: UseWebSocketOptions = {}) => {
  const { userId, onNotification, reconnectInterval = 5000 } = options;
  const [isConnected, setIsConnected] = useState(false);
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimerRef = useRef<NodeJS.Timeout | null>(null);
  const isUnmountedRef = useRef(false);

  const connect = useCallback(() => {
    if (!userId) return;

    if (wsRef.current) {
      wsRef.current.close();
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/notification/${userId}`;

    try {
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        if (!isUnmountedRef.current) {
          setIsConnected(true);
        }
        console.log('WebSocket 连接成功');
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
        if (!isUnmountedRef.current) {
          setIsConnected(false);
        }
        console.log('WebSocket 连接关闭');

        if (reconnectTimerRef.current) {
          clearTimeout(reconnectTimerRef.current);
        }
        if (!isUnmountedRef.current) {
          reconnectTimerRef.current = setTimeout(() => {
            console.log('尝试重新连接...');
            connect();
          }, reconnectInterval);
        }
      };

      ws.onerror = (error) => {
        console.error('WebSocket 错误:', error);
        if (!isUnmountedRef.current) {
          setIsConnected(false);
        }
      };

      wsRef.current = ws;
    } catch (error) {
      console.error('WebSocket 连接失败:', error);
    }
  }, [userId, onNotification, reconnectInterval]);

  const disconnect = useCallback(() => {
    if (reconnectTimerRef.current) {
      clearTimeout(reconnectTimerRef.current);
      reconnectTimerRef.current = null;
    }
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
    if (!isUnmountedRef.current) {
      setIsConnected(false);
    }
  }, []);

  const send = useCallback((data: string) => {
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(data);
    }
  }, []);

  useEffect(() => {
    isUnmountedRef.current = false;
    if (userId) {
      connect();
    }

    return () => {
      isUnmountedRef.current = true;
      disconnect();
    };
  }, [userId, connect, disconnect]);

  return {
    isConnected,
    connect,
    disconnect,
    send,
  };
};
