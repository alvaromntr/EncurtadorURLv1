import { useState } from "react";
import { useAuthStore } from "../store/useAuthStore.js";
import { Link, useNavigate } from "react-router-dom";

export default function LoginPage() {

  const [email, setEmail] = useState("");

  const [pw, setPw] = useState("");

  const navigate = useNavigate();

  const {
    login,
    loading,
    error
  } = useAuthStore();

  const isValidEmail = (email) =>
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

  const isValidPassword = pw.length >= 6;

  const handleSubmit = async (e) => {

    e.preventDefault();

    if (!isValidEmail(email)) {
      return;
    }

    if (!isValidPassword) {
      return;
    }

    try {

      await login(email, pw);

      navigate("/verify-otp");

    } catch (err) {

      console.error(err.message);
    }
  };

  return (

    <div className="min-h-screen flex items-center justify-center bg-base-200">

      <div className="card w-full max-w-md shadow-2xl bg-base-100">

        <div className="card-body">

          <h2 className="text-2xl font-bold text-center">
            Login
          </h2>

          {error && (
            <div className="alert alert-error">
              {error}
            </div>
          )}

          <form
            onSubmit={handleSubmit}
            className="space-y-4"
          >

            <div>

              <input
                type="email"
                placeholder="Email"
                className="input input-bordered w-full"
                value={email}
                onChange={(e) =>
                  setEmail(e.target.value)
                }
                required
              />

              {email && !isValidEmail(email) && (
                <p className="text-red-500 text-sm mt-1">
                  Informe um email válido
                </p>
              )}

            </div>

            <div>

              <input
                type="password"
                placeholder="Senha"
                className="input input-bordered w-full"
                value={pw}
                onChange={(e) =>
                  setPw(e.target.value)
                }
                required
              />

              {pw && !isValidPassword && (
                <p className="text-red-500 text-sm mt-1">
                  A senha deve ter no mínimo 6 caracteres
                </p>
              )}

            </div>

            <button
              type="submit"
              disabled={
                !isValidEmail(email) ||
                !isValidPassword ||
                loading
              }
              className={`btn btn-primary w-full ${
                loading ? "loading" : ""
              }`}
            >
              {loading ? "" : "Entrar"}
            </button>

            <div className="text-center">

              <Link
                to="/signup"
                className="link"
              >
                Criar conta
              </Link>

            </div>

          </form>

        </div>

      </div>

    </div>
  );
}