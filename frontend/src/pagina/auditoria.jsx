import { useEffect, useMemo, useState } from "react";
import Alert from "../componentes/aulaModalAlert";
import Loading from "../componentes/aulaModalCarregando";
import { auditoriaApi } from "../servicos/api";
import { useAuth } from "../context/AuthContext";

const categorias = [
  { value: "", label: "Todas as categorias" },
  { value: "AUTENTICACAO", label: "Autenticação" },
  { value: "AUTORIZACAO", label: "Autorização" },
  { value: "CRIPTOGRAFIA", label: "Criptografia" },
  { value: "USUARIOS", label: "Usuários" },
  { value: "AULAS", label: "Aulas" },
  { value: "LGPD", label: "LGPD" },
];

function formatarData(data) {
  if (!data) return "-";
  return new Date(data).toLocaleString("pt-BR");
}

export default function AuditoriaPagina() {
  const { session, logout } = useAuth();
  const [logs, setLogs] = useState([]);
  const [search, setSearch] = useState("");
  const [categoria, setCategoria] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadLogs() {
    setLoading(true);
    setError("");
    try {
      const data = await auditoriaApi.listar();
      setLogs(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar os logs de auditoria.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadLogs(); }, []);

  const filteredLogs = useMemo(() => {
    const term = search.trim().toLowerCase();
    return logs.filter((log) => {
      if (categoria && log.categoria !== categoria) return false;
      if (!term) return true;
      return [log.email, log.acao, log.descricao, log.ip]
        .filter(Boolean).join(" ").toLowerCase().includes(term);
    });
  }, [logs, search, categoria]);

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
          <a className="navbar-link" href="/home">Home</a>
          <a className="navbar-link" href="/aulas">Aulas</a>
          <a className="navbar-link" href="/usuarios">Usuários</a>
          <a className="navbar-link navbar-link-active" href="/auditoria" aria-current="page">Auditoria</a>
          <a className="navbar-link" href="/politicas">Privacidade</a>
        </div>
        <div className="navbar-user"><div className="user-copy"><strong>{session.usuario.nome}</strong><span>{session.usuario.perfil}</span></div><button className="logout-button" onClick={logout}>Sair</button></div>
      </nav>

      <section className="page">
        <header className="page-header"><div><span className="eyebrow">ROBONET</span><h1>Logs de auditoria</h1><p>Rastreabilidade dos acessos e das operações realizadas no sistema.</p></div><button className="button button-secondary" onClick={loadLogs} disabled={loading}>Atualizar</button></header>
        {error && <Alert type="error" message={error} onClose={() => setError("")} />}

        <section className="content-card">
          <div className="toolbar">
            <div className="search-wrapper"><label htmlFor="search-logs">Pesquisar registros</label><input id="search-logs" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="E-mail, ação, descrição ou IP..." /></div>
            <div className="form-field auditoria-filtro"><label htmlFor="categoria">Categoria</label><select id="categoria" value={categoria} onChange={(event) => setCategoria(event.target.value)}>{categorias.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></div>
            <span className="result-count">{filteredLogs.length} {filteredLogs.length === 1 ? "registro" : "registros"}</span>
          </div>
          {loading ? <Loading text="Carregando logs..." /> : filteredLogs.length === 0 ? <div className="empty-state"><h2>Nenhum registro encontrado.</h2><p>{search || categoria ? "Tente alterar os filtros." : "Ainda não há ações registradas."}</p></div> : (
            <div className="table-container"><table><thead><tr><th>Data/hora</th><th>Usuário</th><th>Categoria</th><th>Ação</th><th>Descrição</th><th>IP</th><th>Resultado</th></tr></thead><tbody>{filteredLogs.map((log) => (
              <tr key={log.id}><td className="auditoria-data">{formatarData(log.dtRegistro)}</td><td>{log.email || "-"}</td><td>{log.categoria}</td><td><strong>{log.acao}</strong></td><td className="description-cell">{log.descricao || "-"}</td><td>{log.ip || "-"}</td><td><span className={`status-pill ${log.sucesso ? "status-active" : "status-inactive"}`}>{log.sucesso ? "Sucesso" : "Falha"}</span></td></tr>
            ))}</tbody></table></div>
          )}
        </section>
      </section>
    </main>
  );
}
