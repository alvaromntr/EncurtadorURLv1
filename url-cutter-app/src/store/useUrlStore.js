import { create } from 'zustand'
import api from '../config/axiosInstance.config.js'

export const useUrlStore = create((set, get) => ({
  shortUrl: null,
  originalUrl: null,
  history: [],
  loading: false,
  error: null,

  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),

  clearState: () =>
    set({
      shortUrl: null,
      originalUrl: null,
      error: null,
    }),

  // 🔗 Criar URL encurtada
  shortenUrl: async (url) => {
    set({ loading: true, error: null })

    try {
      const { data } = await api.post('/api/shorten', {
        url,
      })

      const shortUrl = data.shortUrl || data // depende do seu DTO

      const newEntry = {
        originalUrl: url,
        shortUrl,
        createdAt: new Date().toISOString(),
      }

      set((state) => ({
        shortUrl,
        originalUrl: url,
        history: [newEntry, ...state.history],
        loading: false,
      }))

      return shortUrl
    } catch (err) {
      const message =
        err.response?.data?.message ||
        'Erro ao encurtar URL'

      set({
        error: message,
        loading: false,
      })

      throw new Error(message)
    }
  },

  // 📜 Histórico
  clearHistory: () => set({ history: [] }),
}))