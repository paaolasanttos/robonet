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
const registrosPorPagina = 10;

function formatarData(data) {
  if (!data) return "-";
  return new Date(data).toLocaleString("pt-BR");
}

function valorOuTraco(valor) {
  return valor == null || valor === "" ? "-" : valor;
}

function mascararEmail(email) {
  if (!email) return "-";
  return `${email.slice(0, 5)}${"*".repeat(Math.max(0, email.length - 5))}`;
}

function Paginacao({ pagina, total, aoMudar }) {
  const totalPaginas = Math.max(1, Math.ceil(total / registrosPorPagina));

  if (total === 0) return null;

  return (
    <div className="auditoria-paginacao" aria-label="Paginação dos registros">
      <button type="button" className="button button-secondary button-small" onClick={() => aoMudar(pagina - 1)} disabled={pagina <= 1}>
        Anterior
      </button>
      <span>Página {pagina} de {totalPaginas}</span>
      <button type="button" className="button button-secondary button-small" onClick={() => aoMudar(pagina + 1)} disabled={pagina >= totalPaginas}>
        Próxima
      </button>
    </div>
  );
}

export default function AuditoriaPagina() {
  const { session, logout } = useAuth();
  const [aba, setAba] = useState("acoes");
  const [logs, setLogs] = useState([]);
  const [alteracoes, setAlteracoes] = useState([]);
  const [search, setSearch] = useState("");
  const [categoria, setCategoria] = useState("");
  const [resultado, setResultado] = useState("");
  const [tabela, setTabela] = useState("");
  const [paginaAcoes, setPaginaAcoes] = useState(1);
  const [paginaAlteracoes, setPaginaAlteracoes] = useState(1);
  const [registroSelecionado, setRegistroSelecionado] = useState(null);
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
      if (resultado !== "" && String(log.sucesso) !== resultado) return false;
      if (!term) return true;
      return [log.email, log.acao, log.descricao, log.ip]
        .filter(Boolean).join(" ").toLowerCase().includes(term);
    });
  }, [logs, search, categoria, resultado]);

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
  const logsPaginados = filteredLogs.slice((paginaAcoes - 1) * registrosPorPagina, paginaAcoes * registrosPorPagina);
  const alteracoesPaginadas = filteredAlteracoes.slice((paginaAlteracoes - 1) * registrosPorPagina, paginaAlteracoes * registrosPorPagina);

  useEffect(() => {
    setPaginaAcoes((pagina) => Math.min(pagina, Math.max(1, Math.ceil(filteredLogs.length / registrosPorPagina))));
  }, [filteredLogs.length]);

  useEffect(() => {
    setPaginaAlteracoes((pagina) => Math.min(pagina, Math.max(1, Math.ceil(filteredAlteracoes.length / registrosPorPagina))));
  }, [filteredAlteracoes.length]);

  function pesquisar(event) {
    setSearch(event.target.value);
    setPaginaAcoes(1);
    setPaginaAlteracoes(1);
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
            <div className="search-wrapper"><label htmlFor="search-logs">Pesquisar registros</label><input id="search-logs" type="search" value={search} onChange={pesquisar} placeholder={aba === "acoes" ? "E-mail, ação, descrição ou IP..." : "Tabela, campo, valor ou usuário..."} /></div>
            {aba === "acoes" ? (
              <>
                <div className="form-field auditoria-filtro"><label htmlFor="categoria">Categoria</label><select id="categoria" value={categoria} onChange={(event) => { setCategoria(event.target.value); setPaginaAcoes(1); }}>{categorias.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></div>
                <div className="form-field auditoria-filtro"><label htmlFor="resultado">Resultado</label><select id="resultado" value={resultado} onChange={(event) => { setResultado(event.target.value); setPaginaAcoes(1); }}><option value="">Todos os resultados</option><option value="true">Sucesso</option><option value="false">Falha</option></select></div>
              </>
            ) : (
              <div className="form-field auditoria-filtro"><label htmlFor="tabela">Tabela</label><select id="tabela" value={tabela} onChange={(event) => { setTabela(event.target.value); setPaginaAlteracoes(1); }}><option value="">Todas as tabelas</option>{tabelas.map((item) => <option key={item} value={item}>{item}</option>)}</select></div>
            )}
            <span className="result-count">{total} {total === 1 ? "registro" : "registros"}</span>
          </div>
          {loading ? <Loading text="Carregando logs..." /> : aba === "acoes" ? (
            filteredLogs.length === 0 ? <div className="empty-state"><h2>Nenhum registro encontrado.</h2><p>{search || categoria || resultado ? "Tente alterar os filtros." : "Ainda não há ações registradas."}</p></div> : (
              <>
                <div className="table-container"><table className="tabela-responsiva"><thead><tr><th>Data/hora</th><th>Usuário</th><th>Categoria</th><th>IP</th><th>Resultado</th><th className="actions-column">Ações</th></tr></thead><tbody>{logsPaginados.map((log) => (
                  <tr key={log.id}><td data-label="Data/hora" className="auditoria-data">{formatarData(log.dtRegistro)}</td><td data-label="Usuário">{mascararEmail(log.email)}</td><td data-label="Categoria">{log.categoria}</td><td data-label="IP">{log.ip || "-"}</td><td data-label="Resultado"><span className={`status-pill ${log.sucesso ? "status-active" : "status-inactive"}`}>{log.sucesso ? "Sucesso" : "Falha"}</span></td><td data-label="Ações"><button type="button" className="button button-small button-light" onClick={() => setRegistroSelecionado({ tipo: "acao", dados: log })}>Visualizar</button></td></tr>
                ))}</tbody></table></div>
                <Paginacao pagina={paginaAcoes} total={filteredLogs.length} aoMudar={setPaginaAcoes} />
              </>
            )
          ) : (
            filteredAlteracoes.length === 0 ? <div className="empty-state"><h2>Nenhuma alteração encontrada.</h2><p>{search || tabela ? "Tente alterar os filtros." : "As alterações feitas nas tabelas aparecerão aqui."}</p></div> : (
              <>
                <div className="table-container"><table className="tabela-responsiva"><thead><tr><th>Data/hora</th><th>Tabela</th><th>Registro</th><th>Operação</th><th>Campo</th><th>Usuário</th><th className="actions-column">Ações</th></tr></thead><tbody>{alteracoesPaginadas.map((item) => (
                  <tr key={item.id}><td data-label="Data/hora" className="auditoria-data">{formatarData(item.dtAlteracao)}</td><td data-label="Tabela">{item.tabela}</td><td data-label="Registro">{item.idRegistro ?? "-"}</td><td data-label="Operação">{operacoes[item.operacao] || item.operacao}</td><td data-label="Campo"><strong>{item.campo}</strong></td><td data-label="Usuário">{item.usuario || "-"}</td><td data-label="Ações"><button type="button" className="button button-small button-light" onClick={() => setRegistroSelecionado({ tipo: "alteracao", dados: item })}>Visualizar</button></td></tr>
                ))}</tbody></table></div>
                <Paginacao pagina={paginaAlteracoes} total={filteredAlteracoes.length} aoMudar={setPaginaAlteracoes} />
              </>
            )
          )}
        </section>
      </section>
      {registroSelecionado && (
        <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && setRegistroSelecionado(null)}>
          <div className="modal" role="dialog" aria-modal="true" aria-labelledby="auditoria-detalhes-titulo">
            <div className="modal-header">
              <div>
                <span className="eyebrow">Auditoria</span>
                <h2 id="auditoria-detalhes-titulo">Detalhes do registro</h2>
              </div>
              <button type="button" className="icon-button" onClick={() => setRegistroSelecionado(null)} aria-label="Fechar">×</button>
            </div>
            {registroSelecionado.tipo === "acao" ? (
              <div className="details">
                <div className="detail-item"><span>ID</span><p>{valorOuTraco(registroSelecionado.dados.id)}</p></div>
                <div className="detail-item"><span>Data/hora</span><p>{formatarData(registroSelecionado.dados.dtRegistro)}</p></div>
                <div className="detail-item"><span>Usuário</span><p>{valorOuTraco(registroSelecionado.dados.email)}</p></div>
                <div className="detail-item"><span>Categoria</span><p>{valorOuTraco(registroSelecionado.dados.categoria)}</p></div>
                <div className="detail-item"><span>Resultado</span><p>{registroSelecionado.dados.sucesso ? "Sucesso" : "Falha"}</p></div>
                <div className="detail-item"><span>Ação</span><p>{valorOuTraco(registroSelecionado.dados.acao)}</p></div>
                <div className="detail-item"><span>IP</span><p>{valorOuTraco(registroSelecionado.dados.ip)}</p></div>
                <div className="detail-item detail-description"><span>Descrição</span><p>{valorOuTraco(registroSelecionado.dados.descricao)}</p></div>
              </div>
            ) : (
              <div className="details">
                <div className="detail-item"><span>Data/hora</span><p>{formatarData(registroSelecionado.dados.dtAlteracao)}</p></div>
                <div className="detail-item"><span>Usuário</span><p>{valorOuTraco(registroSelecionado.dados.usuario)}</p></div>
                <div className="detail-item"><span>Campo</span><p>{valorOuTraco(registroSelecionado.dados.campo)}</p></div>
                <div className="detail-item"><span>Tabela</span><p>{valorOuTraco(registroSelecionado.dados.tabela)}</p></div>
                <div className="detail-item"><span>ID</span><p>{valorOuTraco(registroSelecionado.dados.idRegistro)}</p></div>
                <div className="detail-item"><span>Operação</span><p>{operacoes[registroSelecionado.dados.operacao] || valorOuTraco(registroSelecionado.dados.operacao)}</p></div>
                <div className="detail-item detail-description"><span>Valor atual</span><p>{valorOuTraco(registroSelecionado.dados.valorNovo)}</p></div>
                <div className="detail-item detail-description"><span>Valor anterior</span><p>{valorOuTraco(registroSelecionado.dados.valorAnterior)}</p></div>
              </div>
            )}
            <div className="modal-footer"><button type="button" className="button button-primary" onClick={() => setRegistroSelecionado(null)}>Fechar</button></div>
          </div>
        </div>
      )}
    </main>
  );
}
