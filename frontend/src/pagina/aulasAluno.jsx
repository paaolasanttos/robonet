import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import AulaListaAluno from "../componentes/aulaListaAluno";

export default function AulasAlunoPagina() {
  const { session, logout } = useAuth();

  return (
    <main className="app-shell">
      <nav className="navbar" aria-label="Navegação principal">
        <a className="navbar-brand" href="/home" aria-label="ROBONET, página inicial">
          <span className="brand-mark">R</span>
          <span>
            <strong>ROBONET</strong>
            <small>Aluno</small>
          </span>
        </a>

        <div className="navbar-menu">
          <Link className="navbar-link" to="/home">Home</Link>
          <Link className="navbar-link navbar-link-active" to="/aulas-aluno" aria-current="page">Aulas</Link>
          <Link className="navbar-link" to="/politicas">Privacidade</Link>
        </div>

        <div className="navbar-user">
          <div className="user-copy">
            <strong>{session.usuario.nome}</strong>
            <span>{session.usuario.perfil}</span>
          </div>
          <button className="logout-button" onClick={logout}>Sair</button>
        </div>
      </nav>

      <section className="page">
        <AulaListaAluno />
      </section>
    </main>
  );
}
