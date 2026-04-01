import { useNavigationStore } from "../store/useNavigation.js";

export function AuthButtons() {
  const navigateTo = useNavigationStore((state) => state.navigateTo);

  return (
    <div className="flex items-center gap-3">
      <button
        className="btn btn-outline btn-neutral min-w-[100px]"
        onClick={() => navigateTo("/login")}
      >
        Login
      </button>

      <button
        className="btn btn-neutral min-w-[100px]"
        onClick={() => navigateTo("/signup")}
      >
        Criar Conta
      </button>
    </div>
  );
}