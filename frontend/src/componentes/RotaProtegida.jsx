import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function RotaProtegida({ roles }) {
  const { session } = useAuth();
  const location = useLocation();

  if (!session?.token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (roles && !roles.includes(session.usuario?.perfil)) {
    return <Navigate to="/acesso-negado" replace />;
  }

  return <Outlet />;
}
