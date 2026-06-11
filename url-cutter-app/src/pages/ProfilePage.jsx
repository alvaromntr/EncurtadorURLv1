import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { useAuthStore } from '../store/useAuthStore'

import {
  Trash2,
  User,
  ShieldAlert,
  KeyRound
} from 'lucide-react'

import Navbar from '../components/Navbar.jsx'

export default function ProfilePage() {

  const [
    currentPassword,
    setCurrentPassword
  ] = useState('')

  const [
    newPassword,
    setNewPassword
  ] = useState('')

  const [
    confirmPassword,
    setConfirmPassword
  ] = useState('')

  const navigate = useNavigate()

  const {
    user,
    deleteAccount,
    changePassword,
    loading
  } = useAuthStore()

  const [confirmText, setConfirmText] =
    useState('')

  const handleChangePassword =
  async () => {

    if (
      !currentPassword ||
      !newPassword ||
      !confirmPassword
    ) {

      alert(
        'Preencha todos os campos'
      )

      return
    }

    if (
      newPassword !== confirmPassword
    ) {

      alert(
        'As senhas não coincidem'
      )

      return
    }

    try {

      await changePassword(
        currentPassword,
        newPassword
      )

      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')

      alert(
        'Senha alterada com sucesso'
      )

    } catch (err) {

      alert(err.message)
    }
  }

  const handleDelete = async () => {

    if (confirmText !== 'EXCLUIR') {
      return
    }

    try {

      await deleteAccount()

      navigate('/login')

    } catch (err) {

      console.error(err)

      alert(
        'Erro ao excluir conta'
      )
    }
  }

  return (

    <div className="flex flex-col items-center justify-start p-10 gap-6 w-full">

      <Navbar />

      <div
        className="
          min-h-screen
          bg-base-200
          flex
          items-center
          justify-center
          p-6
        "
      >

        <div
          className="
            card
            w-full
            max-w-2xl
            bg-base-100
            shadow-2xl
          "
        >

          <div className="card-body gap-6">

            {/* HEADER */}

            <div
              className="
                flex
                items-center
                gap-4
              "
            >

              <div
                className="
                  w-16
                  h-16
                  rounded-full
                  bg-primary/10
                  flex
                  items-center
                  justify-center
                "
              >

                <User className="w-8 h-8" />

              </div>

              <div>

                <h1
                  className="
                    text-3xl
                    font-bold
                  "
                >
                  Meu Perfil
                </h1>

                <p className="opacity-70">
                  Gerencie sua conta
                </p>

              </div>

            </div>

            {/* USER INFO */}

            <div
              className="
                bg-base-200
                rounded-2xl
                p-5
                space-y-3
              "
            >

              <div>

                <span className="font-semibold">
                  Primeiro nome:
                </span>

                <p>{user?.firstName}</p>

              </div>

              <div>

                <span className="font-semibold">
                  Sobrenome:
                </span>

                <p>{user?.lastName}</p>

              </div>

              <div>

                <span className="font-semibold">
                  Email:
                </span>

                <p>{user?.email}</p>

              </div>

            </div>

            <div
              className="
                border
                rounded-2xl
                p-5
                space-y-4
              "
            >

              <div
                className="
                  flex
                  items-center
                  gap-3
                "
              >

                <KeyRound
                  className="
                    w-6
                    h-6
                  "
                />

                <h2
                  className="
                    text-xl
                    font-bold
                  "
                >
                  Alterar senha
                </h2>

              </div>

              <div className="form-control">

                <label className="label">

                  <span className="label-text">
                    Senha atual
                  </span>

                </label>

                <input
                  type="password"
                  value={currentPassword}
                  onChange={(e) =>
                    setCurrentPassword(
                      e.target.value
                    )
                  }
                  className="
                    input
                    input-bordered
                    w-full
                  "
                />

              </div>

              <div className="form-control">

                <label className="label">

                  <span className="label-text">
                    Nova senha
                  </span>

                </label>

                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) =>
                    setNewPassword(
                      e.target.value
                    )
                  }
                  className="
                    input
                    input-bordered
                    w-full
                  "
                />

              </div>

              <div className="form-control">

                <label className="label">

                  <span className="label-text">
                    Confirmar nova senha
                  </span>

                </label>

                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) =>
                    setConfirmPassword(
                      e.target.value
                    )
                  }
                  className="
                    input
                    input-bordered
                    w-full
                  "
                />

              </div>

              <button
                onClick={handleChangePassword}
                disabled={loading}
                className="
                  btn
                  btn-primary
                  w-full
                "
              >

                {
                  loading
                    ? (
                      <span
                        className="
                          loading
                          loading-spinner
                        "
                      />
                    )
                    : (
                      'Alterar senha'
                    )
                }

              </button>

            </div>

            {/* DANGER ZONE */}

            <div
              className="
                border
                border-error
                rounded-2xl
                p-5
                space-y-4
              "
            >

              <div
                className="
                  flex
                  items-center
                  gap-3
                "
              >

                <ShieldAlert
                  className="
                    text-error
                    w-7
                    h-7
                  "
                />

                <h2
                  className="
                    text-xl
                    font-bold
                    text-error
                  "
                >
                  Zona de perigo
                </h2>

              </div>

              <p className="text-sm opacity-80">

                Esta ação remove permanentemente:

              </p>

              <ul
                className="
                  list-disc
                  list-inside
                  text-sm
                  opacity-80
                "
              >

                <li>Sua conta</li>

                <li>Suas permissões</li>

                <li>Seus códigos OTP</li>

                <li>Seu acesso à plataforma</li>

              </ul>

              <div className="form-control">

                <label className="label">

                  <span className="label-text">
                    Digite
                    {' '}
                    <strong>EXCLUIR</strong>
                    {' '}
                    para confirmar
                  </span>

                </label>

                <input
                  type="text"
                  value={confirmText}
                  onChange={(e) =>
                    setConfirmText(e.target.value)
                  }
                  className="
                    input
                    input-bordered
                    w-full
                  "
                />

              </div>

              <button
                onClick={handleDelete}
                disabled={
                  loading ||
                  confirmText !== 'EXCLUIR'
                }
                className="
                  btn
                  btn-error
                  w-full
                "
              >

                {
                  loading
                    ? (
                      <span
                        className="
                          loading
                          loading-spinner
                        "
                      />
                    )
                    : (
                      <>
                        <Trash2 className="w-5 h-5" />

                        Excluir minha conta
                      </>
                    )
                }

              </button>

            </div>

          </div>

        </div>

      </div>
    </div>
  )
}