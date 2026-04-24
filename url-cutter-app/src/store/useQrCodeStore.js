import { create } from 'zustand'
import api from '../config/axiosInstance.config.js'

export const useQrCodeStore = create((set) => ({
  qrCodeUrl: null,
  loading: false,
  error: null,

  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),

  generateQrCode: async (shortCode) => {
    set({ loading: true, error: null })

    try {
      const response = await api.get(`/qr/qrgenerator/${shortCode}`, {
        responseType: 'blob', // 🔥 ESSENCIAL para imagem
      })

      // Cria uma URL temporária para a imagem
      const imageUrl = URL.createObjectURL(response.data)

      set({
        qrCodeUrl: imageUrl,
        loading: false,
      })

      return imageUrl
    } catch (err) {
      const message = 'Erro ao gerar QR Code'

      set({
        error: message,
        loading: false,
      })

      throw new Error(message)
    }
  },

  clearQrCode: () => {
    set({ qrCodeUrl: null })
  },
}))