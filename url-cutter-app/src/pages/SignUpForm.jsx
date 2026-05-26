import { useState } from "react";
import { useAuthStore } from "../store/useAuthStore.js";
import { useNavigate } from "react-router-dom";

export default function SignUpPage() {
  const [firstName, setFirstName] = useState("");

  const [lastName, setLastName] = useState("");

  const [email, setEmail] = useState("");

  const [pw, setPw] = useState("");

  const [confirmPw, setConfirmPw] = useState("");

  const [localError, setLocalError] = useState("");

  const [showLgpdModal, setShowLgpdModal] = useState(false);

  const [acceptedLgpd, setAcceptedLgpd] = useState(false);

  const navigate = useNavigate();

  const { signup, loading, error } = useAuthStore();

  const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

  const isValidPassword = pw.length >= 6;

  const passwordsMatch = pw === confirmPw;

  const handleSubmit = async (e) => {
    e.preventDefault();

    setLocalError("");

    if (!isValidEmail(email)) {
      setLocalError("Informe um email válido");

      return;
    }

    if (!isValidPassword) {
      setLocalError("A senha deve ter no mínimo 6 caracteres");

      return;
    }

    if (!passwordsMatch) {
      setLocalError("As senhas não coincidem");

      return;
    }

    if (!acceptedLgpd) {
      setShowLgpdModal(true);

      return;
    }

    try {
      await signup(firstName, lastName, email, pw);

      navigate("/verify-otp");
    } catch (err) {
      console.error(err.message);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-base-200">
      <div className="card w-full max-w-md shadow-2xl bg-base-100">
        <div className="card-body">
          <h2 className="text-2xl font-bold text-center">Criar Conta</h2>

          {(error || localError) && (
            <div className="alert alert-error">{localError || error}</div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <input
              type="text"
              placeholder="Primeiro nome"
              className="input input-bordered w-full"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required
            />

            <input
              type="text"
              placeholder="Sobrenome"
              className="input input-bordered w-full"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
            />

            <div>
              <input
                type="email"
                placeholder="Email"
                className="input input-bordered w-full"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
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
                onChange={(e) => setPw(e.target.value)}
                required
              />

              {pw && !isValidPassword && (
                <p className="text-red-500 text-sm mt-1">
                  A senha deve ter no mínimo 6 caracteres
                </p>
              )}
            </div>

            <div>
              <input
                type="password"
                placeholder="Confirmar senha"
                className="input input-bordered w-full"
                value={confirmPw}
                onChange={(e) => setConfirmPw(e.target.value)}
                required
              />

              {confirmPw && !passwordsMatch && (
                <p className="text-red-500 text-sm mt-1">
                  As senhas não coincidem
                </p>
              )}
            </div>

            <button
              type="submit"
              disabled={
                !isValidEmail(email) ||
                !isValidPassword ||
                !passwordsMatch ||
                loading
              }
              className={`btn btn-secondary w-full ${loading ? "loading" : ""}`}
            >
              {loading ? "" : "Cadastrar"}
            </button>
          </form>
        </div>
      </div>

      {/* 📜 Modal LGPD */}
      {showLgpdModal && (
        <div className="modal modal-open">
          <div className="modal-box max-w-2xl">
            <h3 className="font-bold text-xl mb-4">
              Termos de Consentimento LGPD
            </h3>

            <div className="space-y-4 text-sm max-h-80 overflow-y-auto">
              <p>
                Ao criar sua conta, você concorda com a coleta e tratamento dos
                seus dados pessoais conforme a Lei Geral de Proteção de Dados
                (LGPD).
              </p>

              <p>Seus dados serão utilizados para:</p>

              <ul className="list-disc pl-5 space-y-2">
                <li>autenticação e segurança da conta;</li>

                <li>armazenamento de favoritos;</li>

                <li>personalização da experiência;</li>

                <li>comunicação relacionada ao serviço.</li>
              </ul>

              <p>
                Você poderá solicitar alteração ou exclusão dos seus dados a
                qualquer momento.
              </p>
            </div>

            <div className="modal-action">
              <button
                className="btn btn-ghost"
                onClick={() => setShowLgpdModal(false)}
              >
                Cancelar
              </button>

              <button
                className="btn btn-primary"
                onClick={async () => {
                  setAcceptedLgpd(true);

                  setShowLgpdModal(false);

                  try {
                    await signup(firstName, lastName, email, pw);

                    navigate("/verify-otp");
                  } catch (err) {
                    console.error(
                      "Erro ao cadastrar usuário",
                      err.response?.data || err.message,
                    );

                    setLocalError(
                      err.response?.data || "Erro ao cadastrar usuário",
                    );
                  }
                }}
              >
                Aceito os termos
              </button>
            </div>
          </div>

          <div className="modal-backdrop"></div>
        </div>
      )}
    </div>
  );
}
