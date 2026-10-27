import { create } from "zustand";
import { persist } from "zustand/middleware";

interface AdminUiState {
  theme: "light" | "dark";
  collapsed: boolean;
  mobile: boolean;
  toggleTheme: () => void;
  toggleNavigation: (isMobile: boolean) => void;
  closeMobile: () => void;
  resetNavigation: () => void;
}

export const useAdminUiStore = create<AdminUiState>()(
  persist(
    (set) => ({
      theme: localStorage.getItem("futa-admin-theme") === "dark" ? "dark" : "light",
      collapsed: false,
      mobile: false,
      toggleTheme: () => set((state) => ({ theme: state.theme === "dark" ? "light" : "dark" })),
      toggleNavigation: (isMobile) =>
        set((state) => (isMobile ? { collapsed: false, mobile: !state.mobile } : { collapsed: !state.collapsed })),
      closeMobile: () => set({ mobile: false }),
      resetNavigation: () => set({ collapsed: false, mobile: false }),
    }),
    {
      name: "futa-admin-ui",
      partialize: (state) => ({ theme: state.theme }),
    }
  )
);
