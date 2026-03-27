// 搜索历史 Hook
import { useState } from 'react';

const SEARCH_HISTORY_KEY = 'search_history';
const MAX_HISTORY_COUNT = 10;

const loadHistoryFromStorage = (): string[] => {
  const saved = localStorage.getItem(SEARCH_HISTORY_KEY);
  if (saved) {
    try {
      return JSON.parse(saved);
    } catch (e) {
      console.error('解析搜索历史失败:', e);
    }
  }
  return [];
};

export const useSearchHistory = () => {
  const [history, setHistory] = useState<string[]>(loadHistoryFromStorage);

  // 保存到 localStorage
  const saveHistory = (newHistory: string[]) => {
    setHistory(newHistory);
    localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(newHistory));
  };

  // 添加搜索记录
  const addHistory = (keyword: string) => {
    if (!keyword.trim()) return;

    const trimmed = keyword.trim();
    let newHistory = history.filter((item) => item !== trimmed);
    newHistory.unshift(trimmed);

    // 限制数量
    if (newHistory.length > MAX_HISTORY_COUNT) {
      newHistory = newHistory.slice(0, MAX_HISTORY_COUNT);
    }

    saveHistory(newHistory);
  };

  // 移除单条记录
  const removeHistory = (keyword: string) => {
    const newHistory = history.filter((item) => item !== keyword);
    saveHistory(newHistory);
  };

  // 清空历史记录
  const clearHistory = () => {
    saveHistory([]);
  };

  return {
    history,
    addHistory,
    removeHistory,
    clearHistory,
  };
};
