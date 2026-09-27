import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function AcessoNegadoPagina() {
  const { session } = useAuth();
  const destino = "/login";
  const textoDestino = "voltar para login";

  return (
    <main className="login-shell">
      <section className="login-panel">
        <span className="eyebrow">Acesso restrito</span>
        <p>Sua conta não possui autorização para acessar esta área.</p>
        <Link className="button button-primary" to={destino}>{textoDestino}</Link>
      </section>
    </main>
  );
}