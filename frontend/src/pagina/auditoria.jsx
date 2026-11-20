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
  { value: "TURMAS", label: "Turmas" },
  { value: "EQUIPES", label: "Equipes" },
  { value: "TORNEIOS", label: "Torneios" },
];

const operacoes = { INSERT: "Inclusão", UPDATE: "Alteração", DELETE: "Exclusão" };

function formatarData(data) {
  if (!data) return "-";
  return new Date(data).toLocaleString("pt-BR");
}

export default function AuditoriaPagina() {
  const { session, logout } = useAuth();
  const [aba, setAba] = useState("acoes");
  const [logs, setLogs] = useState([]);
  const [alteracoes, setAlteracoes] = useState([]);
  const [search, setSearch] = useState("");
  const [categoria, setCategoria] = useState("");
  const [tabela, setTabela] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadLogs() {
    setLoading(true);
    setError("");
    try {
      const [dadosLogs, dadosAlteracoes] = await Promise.all([auditoriaApi.listar(), auditoriaApi.listarAlteracoes()]);
      setLogs(Array.isArray(dadosLogs) ? dadosLogs : []);
      setAlteracoes(Array.isArray(dadosAlteracoes) ? dadosAlteracoes : []);
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

  const tabelas = useMemo(() => [...new Set(alteracoes.map((item) => item.tabela))].sort(), [alteracoes]);

  const filteredAlteracoes = useMemo(() => {
    const term = search.trim().toLowerCase();
    return alteracoes.filter((item) => {
      if (tabela && item.tabela !== tabela) return false;
      if (!term) return true;
      return [item.tabela, item.campo, item.valorAnterior, item.valorNovo, item.usuario, item.idRegistro]
        .filter((valor) => valor != null).join(" ").toLowerCase().includes(term);
    });
  }, [alteracoes, search, tabela]);

  const total = aba === "acoes" ? filteredLogs.length : filteredAlteracoes.length;

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
          <a className="navbar-link" href="/home">Dashboard</a>
          <a className="navbar-link" href="/aulas">Aulas</a>
          <a className="navbar-link" href="/simulados">Simulados</a>
          <a className="navbar-link" href="/torneios">Torneios</a>
          <a className="navbar-link" href="/turmas">Turmas</a>
          <a className="navbar-link" href="/usuarios">Usuários</a>
          <a className="navbar-link navbar-link-active" href="/auditoria" aria-current="page">Auditoria</a>
        </div>
        <div className="navbar-user"><div className="user-copy"><strong>{session.usuario.nome}</strong><span>{session.usuario.perfil}</span></div><button className="logout-button" onClick={logout}>Sair</button></div>
      </nav>

      <section className="page">
        <header className="page-header"><div><span className="eyebrow">ROBONET</span><h1>Logs de auditoria</h1><p>Rastreabilidade dos acessos, das operações e das alterações de dados realizadas no sistema.</p></div><button className="button button-secondary" onClick={loadLogs} disabled={loading}>Atualizar</button></header>
        {error && <Alert type="error" message={error} onClose={() => setError("")} />}

        <div className="abas" role="tablist" aria-label="Tipos de registro">
          <button type="button" className={`aba ${aba === "acoes" ? "aba-ativa" : ""}`} role="tab" id="aba-acoes" aria-controls="painel-auditoria" aria-selected={aba === "acoes"} onClick={() => setAba("acoes")}>Ações</button>
          <button type="button" className={`aba ${aba === "alteracoes" ? "aba-ativa" : ""}`} role="tab" id="aba-alteracoes" aria-controls="painel-auditoria" aria-selected={aba === "alteracoes"} onClick={() => setAba("alteracoes")}>Histórico de alterações</button>
        </div>

        <section className="content-card" role="tabpanel" id="painel-auditoria" aria-labelledby={aba === "acoes" ? "aba-acoes" : "aba-alteracoes"}>
          <div className="toolbar">
            <div className="search-wrapper"><label htmlFor="search-logs">Pesquisar registros</label><input id="search-logs" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder={aba === "acoes" ? "E-mail, ação, descrição ou IP..." : "Tabela, campo, valor ou usuário..."} /></div>
            {aba === "acoes" ? (
              <div className="form-field auditoria-filtro"><label htmlFor="categoria">Categoria</label><select id="categoria" value={categoria} onChange={(event) => setCategoria(event.target.value)}>{categorias.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></div>
            ) : (
              <div className="form-field auditoria-filtro"><label htmlFor="tabela">Tabela</label><select id="tabela" value={tabela} onChange={(event) => setTabela(event.target.value)}><option value="">Todas as tabelas</option>{tabelas.map((item) => <option key={item} value={item}>{item}</option>)}</select></div>
            )}
            <span className="result-count">{total} {total === 1 ? "registro" : "registros"}</span>
          </div>
          {loading ? <Loading text="Carregando logs..." /> : aba === "acoes" ? (
            filteredLogs.length === 0 ? <div className="empty-state"><h2>Nenhum registro encontrado.</h2><p>{search || categoria ? "Tente alterar os filtros." : "Ainda não há ações registradas."}</p></div> : (
              <div className="table-container"><table className="tabela-responsiva"><thead><tr><th>Data/hora</th><th>Usuário</th><th>Categoria</th><th>Ação</th><th>Descrição</th><th>IP</th><th>Resultado</th></tr></thead><tbody>{filteredLogs.map((log) => (
                <tr key={log.id}><td data-label="Data/hora" className="auditoria-data">{formatarData(log.dtRegistro)}</td><td data-label="Usuário">{log.email || "-"}</td><td data-label="Categoria">{log.categoria}</td><td data-label="Ação"><strong>{log.acao}</strong></td><td data-label="Descrição" className="description-cell">{log.descricao || "-"}</td><td data-label="IP">{log.ip || "-"}</td><td data-label="Resultado"><span className={`status-pill ${log.sucesso ? "status-active" : "status-inactive"}`}>{log.sucesso ? "Sucesso" : "Falha"}</span></td></tr>
              ))}</tbody></table></div>
            )
          ) : (
            filteredAlteracoes.length === 0 ? <div className="empty-state"><h2>Nenhuma alteração encontrada.</h2><p>{search || tabela ? "Tente alterar os filtros." : "As alterações feitas nas tabelas aparecerão aqui."}</p></div> : (
              <div className="table-container"><table className="tabela-responsiva"><thead><tr><th>Data/hora</th><th>Tabela</th><th>Registro</th><th>Operação</th><th>Campo</th><th>Valor anterior</th><th>Valor novo</th><th>Usuário</th></tr></thead><tbody>{filteredAlteracoes.map((item) => (
                <tr key={item.id}><td data-label="Data/hora" className="auditoria-data">{formatarData(item.dtAlteracao)}</td><td data-label="Tabela">{item.tabela}</td><td data-label="Registro">{item.idRegistro ?? "-"}</td><td data-label="Operação">{operacoes[item.operacao] || item.operacao}</td><td data-label="Campo"><strong>{item.campo}</strong></td><td data-label="Valor anterior" className="description-cell">{item.valorAnterior ?? "-"}</td><td data-label="Valor novo" className="description-cell">{item.valorNovo ?? "-"}</td><td data-label="Usuário">{item.usuario || "-"}</td></tr>
              ))}</tbody></table></div>
            )
          )}
        </section>
      </section>
    </main>
  );
}
