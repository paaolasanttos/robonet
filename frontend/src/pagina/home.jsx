import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function HomePagina() {
  const { session, logout } = useAuth();
  const perfil = String(session?.usuario?.perfil || "").toLowerCase();

  const cards = perfil === "aluno"
    ? [
        { to: "/aulas-aluno", title: "Aulas para alunos", description: "Acesse as aulas disponíveis e acompanhe o conteúdo." },
      ]
    : perfil === "professor"
      ? [
          { to: "/aulas", title: "Aulas", description: "Cadastre, edite e acompanhe as aulas da plataforma." },
        ]
      : [
          { to: "/aulas", title: "Aulas", description: "Gerencie e acompanhe as aulas cadastradas." },
          { to: "/usuarios", title: "Usuários", description: "Cadastre e mantenha os perfis de acesso." },
          { to: "/auditoria", title: "Auditoria", description: "Consulte os registros do sistema." },
        ];

  return (
    <main className="app-shell">
      <nav className="navbar" aria-label="Navegação principal">
        <a className="navbar-brand" href="/home" aria-label="ROBONET, página inicial">
          <span className="brand-mark">R</span>
          <span>
            <strong>ROBONET</strong>
            <small>Home</small>
          </span>
        </a>

        <div className="navbar-menu">
          <Link className="navbar-link navbar-link-active" to="/home" aria-current="page">Home</Link>
          {perfil === "aluno" ? <Link className="navbar-link" to="/aulas-aluno">Aulas</Link> : <Link className="navbar-link" to="/aulas">Aulas</Link>}
          {perfil !== "aluno" && <Link className="navbar-link" to="/usuarios">Usuários</Link>}
          {perfil !== "aluno" && <Link className="navbar-link" to="/auditoria">Auditoria</Link>}
          <Link className="navbar-link" to="/politicas">Privacidade</Link>
        </div>

        <div className="navbar-user">
          <div className="user-copy">
            <strong>{session?.usuario?.nome || "Usuário"}</strong>
            <span>{session?.usuario?.perfil || "perfil"}</span>
          </div>
          <button className="logout-button" onClick={logout}>Sair</button>
        </div>
      </nav>

      <section className="page">
        <header className="page-header">
          <div>
            <span className="eyebrow">Bem-vindo</span>
            <h1>ROBONET</h1>
          </div>
        </header>

        <section className="content-card" style={{ display: "grid", gap: "18px", gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))" }}>
          {cards.map((card) => (
            <Link key={card.title} to={card.to} style={{ textDecoration: "none", color: "inherit" }}>
              <article className="content-card" style={{ padding: "22px", borderRadius: "16px", minHeight: "180px", display: "flex", flexDirection: "column", justifyContent: "space-between" }}>
                <div>
                  <h2 style={{ margin: 0, marginBottom: "10px" }}>{card.title}</h2>
                  <p style={{ margin: 0, color: "#475467", lineHeight: 1.5 }}>{card.description}</p>
                </div>
                <span className="button button-primary" style={{ width: "fit-content", marginTop: "18px" }}>Acessar</span>
              </article>
            </Link>
          ))}
        </section>
      </section>
    </main>
  );
}
