import React, { useEffect, useState } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'

import { useAuthStore } from './store/useAuthStore'

import Navbar from './components/Navbar.jsx'
import Home from './pages/Home.jsx'
import LoginForm from './pages/LoginForm.jsx'
import SignUpForm from './pages/SignUpForm.jsx'
import Shortener from './pages/Shortener.jsx'

// 🔒 Componente de rota protegida
function PrivateRoute({ children }) {
  const token = useAuthStore((state) => state.token)
  return token ? children : <Navigate to="/login" replace />
}

// 🔓 Rota pública (bloqueia acesso se já logado)
function PublicRoute({ children }) {
  const token = useAuthStore((state) => state.token)
  return !token ? children : <Navigate to="/" replace />
}

export default function App() {
  const { setUser, setToken } = useAuthStore()
  const [initialized, setInitialized] = useState(false)

  useEffect(() => {

    const storedUser = localStorage.getItem('user')
    const storedToken = localStorage.getItem('token')

    if (storedUser && storedToken) {
      setUser(JSON.parse(storedUser))
      setToken(storedToken)
    }

    setInitialized(true)
  }, [setUser, setToken])

  if (!initialized) {
    return (
      <div className="flex items-center justify-center h-screen">
        Carregando...
      </div>
    )
  }

  return (
    <div className="flex flex-col h-screen w-screen">
      
      {/* Navbar sempre visível (pode esconder se quiser em login) */}
      <Navbar />

      <Routes>

        <Route
          path="/"
          element={
            <PrivateRoute>
              <Shortener />
            </PrivateRoute>
          }
        />

        {/* 🔓 Rotas públicas */}
        <Route
          path="/login"
          element={
            <PublicRoute>
              <LoginForm />
            </PublicRoute>
          }
        />

        <Route
          path="/signup"
          element={
            <PublicRoute>
              <SignUpForm />
            </PublicRoute>
          }
        />

        {/* 🔁 fallback */}
        <Route path="*" element={<Navigate to="/" />} />

      </Routes>
    </div>
  )
}