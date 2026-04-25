import { defineStore } from "pinia";
import { authApi } from "@/api/modules/auth";
import {
  clearAuthSession,
  readAuthSession,
  saveAuthSession,
} from "@/utils/auth";

function defaultState() {
  return {
    token: "",
    role: "",
    displayName: "",
    profile: null,
    initialized: false,
  };
}

export const useUserStore = defineStore("user", {
  state: () => defaultState(),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => state.role === "ADMIN",
  },
  actions: {
    hydrate() {
      const session = readAuthSession();
      this.token = session.token;
      this.role = session.role;
      this.displayName = session.displayName;
      this.profile = session.profile;
      this.initialized = true;
    },
    setSession(session) {
      // 后端返回的字段可能是 accessToken 或 token，需要兼容处理
      const normalized = saveAuthSession({
        token: session.accessToken || session.token || "",
        role: session.role || "",
        displayName: session.name || session.displayName || "",
        profile: session.profile || null,
      });
      this.token = normalized.token;
      this.role = normalized.role;
      this.displayName = normalized.displayName;
      this.profile = normalized.profile;
      this.initialized = true;
    },
    clearSession() {
      clearAuthSession();
      Object.assign(this, defaultState(), { initialized: true });
    },
    async login(payload) {
      const session = await authApi.login(payload);
      this.setSession(session);
      return session;
    },
    async logout() {
      try {
        if (authApi.logout) {
          await authApi.logout();
        }
      } catch (e) {
        console.warn("Backend logout failed", e);
      } finally {
        this.clearSession();
      }
    },
  },
});
