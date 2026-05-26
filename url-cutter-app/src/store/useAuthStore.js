import { create } from 'zustand'
import api from '../config/axiosInstance.config.js'

const errorMessages = {
  INVALID_CREDENTIALS:
    'Email ou senha inválidos',

  EMAIL_NOT_VERIFIED:
    'Seu email ainda não foi verificado',

  INVALID_CODE:
    'Código inválido',

  EMAIL_ALREADY_EXISTS:
    'Este email já está cadastrado',

  USER_NOT_FOUND:
    'Usuário não encontrado'
}

const resolveErrorMessage = (err, fallback) => {

  const rawMessage =
    typeof err.response?.data === 'string'
      ? err.response.data
      : err.response?.data?.message

  return (
    errorMessages[rawMessage] ||
    fallback
  )
}

export const useAuthStore = create((set, get) => ({

  user: JSON.parse(localStorage.getItem('user')) || null,

  token: localStorage.getItem('token') || null,

  role: localStorage.getItem('role') || null,

  pendingEmail:
    localStorage.getItem('pendingEmail') || null,

  otpType:
    localStorage.getItem('otpType') || null,

  loading: false,

  error: null,

  // ================= SETTERS =================

  setUser: (user) => {

    if (user) {

      const normalizedUser = {
        ...user,
        firstName: user.firstName || '',
        lastName: user.lastName || ''
      }

      localStorage.setItem(
        'user',
        JSON.stringify(normalizedUser)
      )

      set({
        user: normalizedUser
      })

    } else {

      localStorage.removeItem('user')

      set({
        user: null
      })
    }
  },

  setToken: (token) => {

    if (token) {
      localStorage.setItem('token', token)
    } else {
      localStorage.removeItem('token')
    }

    set({ token })
  },

  setRole: (role) => {

    if (role) {
      localStorage.setItem('role', role)
    } else {
      localStorage.removeItem('role')
    }

    set({ role })
  },

  setPendingEmail: (email) => {

    if (email) {

      localStorage.setItem(
        'pendingEmail',
        email
      )

    } else {

      localStorage.removeItem(
        'pendingEmail'
      )
    }

    set({
      pendingEmail: email
    })
  },

  setOtpType: (type) => {

    if (type) {

      localStorage.setItem(
        'otpType',
        type
      )

    } else {

      localStorage.removeItem(
        'otpType'
      )
    }

    set({
      otpType: type
    })
  },

  setError: (error) => set({ error }),

  setLoading: (loading) => set({ loading }),

  // ================= LOGIN =================

  login: async (email, pw) => {

    set({
      loading: true,
      error: null
    })

    try {

      await api.post(
        '/auth/login',
        {
          email,
          pw
        }
      )

      get().setPendingEmail(email)

      get().setOtpType('login')

      set({
        loading: false
      })

      return {
        requires2FA: true
      }

    } catch (err) {

      const message =
        resolveErrorMessage(
          err,
          'Erro ao realizar login'
        )

      set({
        error: message,
        loading: false
      })

      throw new Error(message)
    }
  },

  // ================= SIGNUP =================

  signup: async (
    firstName,
    lastName,
    email,
    pw
  ) => {

    set({
      loading: true,
      error: null
    })

    try {

      await api.post(
        '/auth/signup',
        {
          firstName,
          lastName,
          email,
          pw
        }
      )

      get().setPendingEmail(email)

      get().setOtpType('signup')

      set({
        loading: false
      })

      return {
        requires2FA: true
      }

    } catch (err) {

      const message =
        resolveErrorMessage(
          err,
          'Erro ao cadastrar usuário'
        )

      set({
        error: message,
        loading: false
      })

      throw new Error(message)
    }
  },

  // ================= VERIFY OTP =================

  verifyOtp: async (code) => {

    set({
      loading: true,
      error: null
    })

    try {

      const endpoint =
        get().otpType === 'signup'
          ? '/auth/verify-signup'
          : '/auth/verify-2fa'

      const { data } = await api.post(
        endpoint,
        {
          email: get().pendingEmail,
          code
        }
      )

      // Compatibilidade entre os dois endpoints
      const user =
        data.user ||
        data.userResponseDTO

      const role =
        data.role?.startsWith('ROLE_')
          ? data.role
          : `ROLE_${data.role}`

      get().setToken(data.token)

      get().setRole(role)

      get().setUser({
        ...user,
        firstName:
          user?.firstName || '',
        lastName:
          user?.lastName || ''
      })

      get().setPendingEmail(null)

      get().setOtpType(null)

      set({
        loading: false
      })

      return data

    } catch (err) {

      const message =
        resolveErrorMessage(
          err,
          'Código inválido'
        )

      set({
        error: message,
        loading: false
      })

      throw new Error(message)
    }
  },

  // ================= LOGOUT =================

  logout: () => {

    get().setToken(null)

    get().setRole(null)

    get().setUser(null)

    get().setPendingEmail(null)

    get().setOtpType(null)

    set({
      error: null
    })
  },

  // ================= DELETE ACCOUNT =================

  deleteAccount: async () => {

    set({
      loading: true,
      error: null
    })

    try {

      await api.delete(
        '/auth/delete',
        {
          headers: {
            Authorization:
              `Bearer ${get().token}`
          }
        }
      )

      get().logout()

      set({
        loading: false
      })

    } catch (err) {

      const message =
        resolveErrorMessage(
          err,
          'Erro ao excluir conta'
        )

      set({
        error: message,
        loading: false
      })

      throw new Error(message)
    }
  },

  // ================= HELPERS =================

  isAuthenticated: () =>
    !!get().token,

  isActive: () =>
    get().user?.status === 'ACTIVE',
}))