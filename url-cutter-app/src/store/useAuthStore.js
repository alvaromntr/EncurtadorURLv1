import { create } from 'zustand'
import api from '../config/axiosInstance.config.js'

export const useAuthStore = create((set, get) => ({
  user: JSON.parse(localStorage.getItem('user')) || null,
  token: localStorage.getItem('token') || null,
  role: localStorage.getItem('role') || null,
  loading: false,
  error: null,

  setUser: (user) => {
    localStorage.setItem('user', JSON.stringify(user))
    set({ user })
  },
  setToken: (token) => {
    if (token) {
      localStorage.setItem('token', token)
    } else {
      localStorage.removeItem('token')
    }
    set({ token })
  },

  setError: (error) => set({ error }),

  setLoading: (loading) => set({ loading }),

  login: async (mail, pw) => {
    set({ loading: true, error: null })

    try {
      const { data } = await api.post('/auth/login', { mail, pw })

      set({
        token: data.token,
        role: data.role,
        user: data.userResponseDTO,
        loading: false,
      })

      localStorage.setItem('token', data.token)
      localStorage.setItem('role', data.role)
      localStorage.setItem('user', JSON.stringify(data.userResponseDTO))

      console.log('Login successful:', data)

      //console.log('Stored role in localStorage:', get().role)

      return data
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.response?.status === 401
          ? 'Credenciais inválidas'
          : 'Erro ao realizar login'

      set({
        error: message,
        loading: false,
        user: null,
        token: null,
        role: null,
      })

      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('user')

      throw new Error(message)
    }
  },

  signup: async (mail, pw) => {
    set({ loading: true, error: null })

    try {
      const { data } = await api.post('/auth/signup', { mail, pw })

      set({
        token: data.token ?? null,
        user: data.userResponseDTO ?? null,
        loading: false,
      })

      if (data.token) localStorage.setItem('token', data.token)
      if (data.userResponseDTO)
        localStorage.setItem('user', JSON.stringify(data.userResponseDTO))

      return data
    } catch (err) {
      const message =
        err.response?.data ||
        err.response?.data?.message ||
        'Erro ao cadastrar usuário'

      set({
        error: message,
        loading: false,
      })

      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('role')

      throw new Error(message)
    }
  },

  logout: () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('role')
    set({
      user: null,
      token: null,
      error: null,
    })
  },

  isAuthenticated: () => !!get().token,
  isActive: () => get().user?.status === 'ACTIVE',
}))
