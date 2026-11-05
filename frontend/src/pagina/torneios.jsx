import { useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import TorneioLista from "../componentes/torneioLista";
import TorneioDetalhe from "../componentes/torneioDetalhe";
import TorneioEquipes from "../componentes/torneioEquipes";

export default function TorneiosPagina() {
  const { session, logout } = useAuth();
  const perfil = String(session?.usuario?.perfil || "").toLowerCase();
  const [aba, setAba] = useState("torneios");
  const [torneioId, setTorneioId] = useState(null);

  function trocarAba(novaAba) {
    setAba(novaAba);
    setTorneioId(null);
  }

  return (
    <main className="app-shell">
      <nav className="navbar" aria-label="Navegação principal">
        <a className="navbar-brand" href="/home" aria-label="ROBONET, página inicial">
          <span className="brand-mark">R</span>
          <span>
            <strong>ROBONET</strong>
          </span>
        </a>

        <div className="navbar-menu">
          <Link className="navbar-link" to="/home">Home</Link>
          {perfil === "aluno" ? <Link className="navbar-link" to="/aulas-aluno">Aulas</Link> : <Link className="navbar-link" to="/aulas">Aulas</Link>}
          {perfil === "aluno" ? <Link className="navbar-link" to="/simulados-aluno">Simulados</Link> : <Link className="navbar-link" to="/simulados">Simulados</Link>}
          <Link className="navbar-link navbar-link-active" to="/torneios" aria-current="page">Torneios</Link>
          {perfil === "admin" && <Link className="navbar-link" to="/turmas">Turmas</Link>}
          {perfil === "admin" && <Link className="navbar-link" to="/usuarios">Usuários</Link>}
          {perfil === "admin" && <Link className="navbar-link" to="/auditoria">Auditoria</Link>}
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
        <header className="page-header">
          <div>
            <span className="eyebrow">ROBONET</span>
            <h1>Torneios</h1>
            <p>{perfil === "aluno" ? "Acompanhe o chaveamento e os vencedores das lutas." : "Organize as equipes e os torneios no formato mata-mata."}</p>
          </div>
        </header>

        {perfil !== "aluno" && (
          <div className="abas" role="tablist" aria-label="Seções de torneios">
            <button className={`aba ${aba === "torneios" ? "aba-ativa" : ""}`} role="tab" aria-selected={aba === "torneios"} onClick={() => trocarAba("torneios")}>Torneios</button>
            <button className={`aba ${aba === "equipes" ? "aba-ativa" : ""}`} role="tab" aria-selected={aba === "equipes"} onClick={() => trocarAba("equipes")}>Equipes</button>
          </div>
        )}

        {aba === "equipes" && perfil !== "aluno" ? (
          <TorneioEquipes perfil={perfil} />
        ) : torneioId ? (
          <TorneioDetalhe id={torneioId} perfil={perfil} onVoltar={() => setTorneioId(null)} />
        ) : (
          <TorneioLista perfil={perfil} onAbrir={setTorneioId} />
        )}
      </section>
    </main>
  );
}
