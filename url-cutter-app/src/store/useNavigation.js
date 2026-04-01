import { create } from "zustand";

export const useNavigationStore = create((set) => ({
  path: null,
  navigateTo: (path) => set({ path }),
  clearPath: () => set({ path: null }),
}));
