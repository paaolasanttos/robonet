import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function RotaProtegida({ roles }) {
  const { session } = useAuth();
  const location = useLocation();
  const perfil = String(session?.usuario?.perfil || "").trim().toLowerCase();

  if (!session?.token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (perfil !== "admin" && !session.usuario?.termoAceito) {
    return <Navigate to="/termo" replace state={{ from: location.pathname }} />;
  }

  if (roles) {
    const permissoes = roles.map((role) => String(role).trim().toLowerCase());
    const permitido = perfil === "admin" || permissoes.includes(perfil);

    if (!permitido) {
      return <Navigate to="/acesso-negado" replace state={{ from: location.pathname }} />;
    }
  }

  return <Outlet />;
}
