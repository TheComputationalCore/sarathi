import { create } from "zustand";

export const useAppStore = create((set) => ({
  authUser: null,
  sidebarOpen: true,
  globalLoading: false,
  selectedThemes: [],

  setAuthUser: (user) => set({ authUser: user }),
  toggleSidebar: () =>
    set((s) => ({ sidebarOpen: !s.sidebarOpen })),
  setGlobalLoading: (val) => set({ globalLoading: val }),
  setSelectedThemes: (themes) =>
    set({ selectedThemes: themes }),
}));