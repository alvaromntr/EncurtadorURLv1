import { useState } from "react";
import { useAuthStore } from "../store/useAuthStore.js";
import { useNavigate } from "react-router-dom";

export default function VerifyOtpPage() {

  const [code, setCode] = useState("");

  const navigate = useNavigate();

  const {
    verifyOtp,
    pendingEmail,
    loading,
    error
  } = useAuthStore();

  const handleSubmit = async (e) => {

    e.preventDefault();

    try {

      await verifyOtp(code)

      alert("Autenticação concluída!")

      navigate("/")

    } catch (err) {

      console.error(err.message)
    }
  }

  return (

    <div className="min-h-screen flex items-center justify-center bg-base-200">

      <div className="card w-full max-w-md shadow-2xl bg-base-100">

        <div className="card-body">

          <h2 className="text-2xl font-bold text-center">
            Verificação
          </h2>

          <p className="text-center text-sm opacity-70">

            Digite o código enviado para:

            <br />

            <strong>{pendingEmail}</strong>

          </p>

          {error && (
            <div className="alert alert-error">
              {error}
            </div>
          )}

          <form
            onSubmit={handleSubmit}
            className="space-y-4"
          >

            <input
              type="text"
              placeholder="Código OTP"
              className="input input-bordered w-full text-center text-lg tracking-widest"
              value={code}
              onChange={(e) =>
                setCode(e.target.value)
              }
              maxLength={6}
            />

            <button
              className={`btn btn-primary w-full ${
                loading ? 'loading' : ''
              }`}
            >
              Verificar
            </button>

          </form>

        </div>

      </div>

    </div>
  )
}