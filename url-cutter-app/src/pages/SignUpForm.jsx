import { useState } from "react";
import { useAuthStore } from "../store/useAuthStore.js";

// ================= SIGNUP PAGE =================
export default function SignUpPage() {
  const [mail, setMail] = useState("");
  const [pw, setPw] = useState("");

  const { signup, loading, error } = useAuthStore();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await signup(mail, pw);
      alert("Conta criada com sucesso!");
    } catch (err) {
      console.error(err.message);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-base-200">
      <div className="card w-full max-w-md shadow-2xl bg-base-100">
        <div className="card-body">
          <h2 className="text-2xl font-bold text-center">Criar Conta</h2>

          {error && (
            <div className="alert alert-error text-sm">{error}</div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="form-control">
              <label className="label">Email</label>
              <input
                type="email"
                className="input input-bordered"
                value={mail}
                onChange={(e) => setMail(e.target.value)}
                required
              />
            </div>

            <div className="form-control">
              <label className="label">Senha</label>
              <input
                type="password"
                className="input input-bordered"
                value={pw}
                onChange={(e) => setPw(e.target.value)}
                required
              />
            </div>

            <button
              type="submit"
              className={`btn btn-secondary w-full ${loading && "loading"}`}
              disabled={loading}
            >
              Cadastrar
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}