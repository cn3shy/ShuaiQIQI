// 认证状态管理 - 使用Zustand
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User, AuthResponse } from '@types';

interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // Actions
  setAuth: (auth: AuthResponse) => void;
  setUser: (user: User | null) => void;
  clearAuth: () => void;
  setLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,

      setAuth: (auth) => {
        localStorage.setItem('token', auth.token);
        localStorage.setItem('refreshToken', auth.refreshToken);
        set({
          user: auth.user,
          token: auth.token,
          refreshToken: auth.refreshToken,
          isAuthenticated: true,
          isLoading: false,
        });
      },

      setUser: (user) => set((state) => ({ user, isAuthenticated: user !== null })),

      clearAuth: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        set({
          user: null,
          token: null,
          refreshToken: null,
          isAuthenticated: false,
          isLoading: false,
        });
      },

      setLoading: (loading) => set({ isLoading: loading }),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        refreshToken: state.refreshToken,
      }),
    }
  )
);