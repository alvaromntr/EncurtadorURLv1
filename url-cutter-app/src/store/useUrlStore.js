import { create } from 'zustand'
import api from '../config/axiosInstance.config.js'

export const useUrlStore = create((set, get) => ({
  shortUrl: null,
  originalUrl: null,
  history: [],
  clicks: [],        // 👈 adicionar
  clickCounts: {},
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
      const { data } = await api.post('/api/shorten', { url })

      const newEntry = {
        id: data.id, // 👈 importante para delete
        shortUrl: data.shortUrl,
        originalUrl: data.originalUrl,
        clickCount: data.clickCount,
        createdAt: data.createdAt,
      }

      set((state) => ({
        shortUrl: data.shortUrl,
        originalUrl: data.originalUrl,
        history: [newEntry, ...state.history],
        loading: false,
      }))

      return data.shortUrl
    } catch (err) {
      const message =
        err.response?.data?.message || 'Erro ao encurtar URL'

      set({
        error: message,
        loading: false,
      })

      throw new Error(message)
    }
  },

  fetchUrls: async () => {
    set({ loading: true, error: null })

    try {
      const { data } = await api.get('/api/my-urls')

      const urls = Array.isArray(data)
        ? data
        : data?.content || data?.data || []

      set({
        history: urls, // 🔥 SEM clickCount aqui
        loading: false,
      })

      // 🔥 depois de carregar URLs, busca os cliques
      get().refreshClicks()

    } catch (err) {
      set({
        error:
          err.response?.data?.message || 'Erro ao buscar URLs',
        loading: false,
      })
    }
  },

  // ❌ Deletar URL
  deleteUrl: async (id) => {
    set({ loading: true, error: null })

    try {
      await api.delete(`/api/${id}`)

      set((state) => ({
        history: state.history.filter((item) => item.id !== id),
        loading: false,
      }))
    } catch (err) {
      const message =
        err.response?.data?.message || 'Erro ao deletar URL'

      set({
        error: message,
        loading: false,
      })
    }
  },

  fetchClicksByUrl: async (urlId) => {
    set({ loading: true, error: null })

    try {
      const { data } = await api.get(`/api/clicks/url/${urlId}`)

      set({
        clicks: data,
        loading: false,
      })
    } catch (err) {
      const message =
        err.response?.data?.message || 'Erro ao buscar cliques'

      set({
        error: message,
        loading: false,
      })
    }
  },

    fetchClicksByUrl: async (urlId) => {
    set({ loading: true, error: null })

    try {

      const { data } =
        await api.get(`/api/clicks/url/${urlId}`)

      set({
        clicks: data,
        loading: false,
      })

    } catch (err) {

      set({
        error:
          err.response?.data?.message ||
          'Erro ao buscar cliques',
        loading: false,
      })
    }
  },

  refreshClicks: async () => {
    try {
      const { data } = await api.get('/api/clicks')

      const counts = {}

      for (const click of data) {
        counts[click.urlId] = (counts[click.urlId] || 0) + 1
      }

      set({ clickCounts: counts })
    } catch (err) {
      console.error('Erro ao atualizar cliques', err)
    }
  },

  // 📜 Limpar histórico local
  clearHistory: () => set({ history: [] }),
}))