import { Link } from "react-router-dom";

export default function TorneiosPagina() {
  return (
    <main className="app-shell">
      <nav className="navbar" aria-label="Navegação principal">
        <Link className="navbar-brand" to="/home" aria-label="ROBONET, página inicial">
          <span className="brand-mark">R</span>
          <span>
            <strong>ROBONET</strong>
            <small>Torneios</small>
          </span>
        </Link>
        <div className="navbar-menu">
          <Link className="navbar-link" to="/home">Home</Link>
          <Link className="navbar-link navbar-link-active" to="/torneios" aria-current="page">Torneios</Link>
        </div>
      </nav>

      <section className="page">
        <header className="page-header">
          <div>
            <span className="eyebrow">Torneios</span>
            <h1>Módulo em desenvolvimento</h1>
          </div>
        </header>
        <section className="content-card">
          <p>O gerenciamento de torneios ainda não está disponível.</p>
        </section>
      </section>
    </main>
  );
}
