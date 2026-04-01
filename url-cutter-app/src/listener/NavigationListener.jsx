import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useNavigationStore } from "../store/useNavigation.js";

export function NavigationListener() {
  const navigate = useNavigate();
  const { path, clearPath } = useNavigationStore();

  useEffect(() => {
    if (path) {
      navigate(path);
      clearPath(); // evita navegação repetida
    }
  }, [path, navigate, clearPath]);

  return null; // componente invisível
}
