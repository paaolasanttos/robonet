import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Alert from "../componentes/aulaModalAlert";
import ConfirmModal from "../componentes/aulaModalConfirmar";
import Loading from "../componentes/aulaModalCarregando";
import SimuladoModalForm from "../componentes/simuladoModalForm";
import { aulasApi, simuladosApi } from "../servicos/api";

export default function SimuladosPagina() {
  const { session, logout } = useAuth();
  const [simulados, setSimulados] = useState([]);
  const [temas, setTemas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");
  const [search, setSearch] = useState("");
  const [formOpen, setFormOpen] = useState(false);
  const [editingSimulado, setEditingSimulado] = useState(null);
  const [selectedSimulado, setSelectedSimulado] = useState(null);

  const carregar = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const [lista, listaTemas] = await Promise.all([simuladosApi.listar(), aulasApi.listarTemas()]);
      setSimulados(Array.isArray(lista) ? lista : []);
      setTemas(Array.isArray(listaTemas) ? listaTemas : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar os simulados.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { carregar(); }, [carregar]);
  useEffect(() => {
    if (!feedback) return undefined;
    const timer = setTimeout(() => setFeedback(""), 4000);
    return () => clearTimeout(timer);
  }, [feedback]);

  const filtrados = useMemo(() => {
    const termo = search.trim().toLowerCase();
    return termo ? simulados.filter((item) => `${item.titulo} ${item.tema}`.toLowerCase().includes(termo)) : simulados;
  }, [simulados, search]);

  function abrirFormulario(simulado = null) {
    setEditingSimulado(simulado);
    setError("");
    setFormOpen(true);
  }

  async function salvar(dados) {
    setActionLoading(true);
    setError("");
    try {
      if (editingSimulado) {
        const atualizado = await simuladosApi.atualizar(editingSimulado.id, dados);
        setSimulados((atuais) => atuais.map((item) => item.id === atualizado.id ? atualizado : item));
        setFeedback("Simulado atualizado com sucesso.");
      } else {
        const criado = await simuladosApi.criar(dados);
        setSimulados((atuais) => [...atuais, criado]);
        setFeedback("Simulado cadastrado com sucesso.");
      }
      setFormOpen(false);
      setEditingSimulado(null);
    } catch (err) {
      setError(err.message || "Não foi possível salvar o simulado.");
    } finally {
      setActionLoading(false);
    }
  }

  async function excluir() {
    if (!selectedSimulado) return;
    setActionLoading(true);
    setError("");
    try {
      await simuladosApi.excluir(selectedSimulado.id);
      setSimulados((atuais) => atuais.filter((item) => item.id !== selectedSimulado.id));
      setSelectedSimulado(null);
      setFeedback("Simulado excluído com sucesso.");
    } catch (err) {
      setError(err.message || "Não foi possível excluir o simulado.");
    } finally {
      setActionLoading(false);
    }
  }

  return (
    <main className="app-shell">
      <nav className="navbar" aria-label="Navegação principal">
        <a className="navbar-brand" href="/home" aria-label="ROBONET, página inicial"><span className="brand-mark">R</span><span><strong>ROBONET</strong></span></a>
        <div className="navbar-menu">
          <Link className="navbar-link" to="/home">Home</Link>
          <Link className="navbar-link" to="/aulas">Aulas</Link>
          <Link className="navbar-link navbar-link-active" to="/simulados" aria-current="page">Simulados</Link>
          <Link className="navbar-link" to="/torneios">Torneios</Link>
          {session.usuario.perfil === "admin" && <Link className="navbar-link" to="/turmas">Turmas</Link>}
          {session.usuario.perfil === "admin" && <Link className="navbar-link" to="/usuarios">Usuários</Link>}
          {session.usuario.perfil === "admin" && <Link className="navbar-link" to="/auditoria">Auditoria</Link>}
          <Link className="navbar-link" to="/politicas">Privacidade</Link>
        </div>
        <div className="navbar-user"><div className="user-copy"><strong>{session.usuario.nome}</strong><span>{session.usuario.perfil}</span></div><button className="logout-button" onClick={logout}>Sair</button></div>
      </nav>

      <section className="page">
        <header className="page-header">
          <div><span className="eyebrow">ROBONET</span><h1>Cadastro de Simulados</h1></div>
          <button className="button button-primary" onClick={() => abrirFormulario()}>+ Novo Simulado</button>
        </header>
        {feedback && <Alert type="success" message={feedback} onClose={() => setFeedback("")} />}
        {error && !formOpen && <Alert type="error" message={error} onClose={() => setError("")} />}
        <section className="content-card">
          <div className="toolbar">
            <div className="search-wrapper">
              <label htmlFor="search-simulados">Pesquisar simulados</label>
              <input id="search-simulados" type="search" placeholder="Digite título ou tema..." value={search} onChange={(event) => setSearch(event.target.value)} />
            </div>
            <span className="result-count">{filtrados.length} {filtrados.length === 1 ? "simulado" : "simulados"}</span>
          </div>
          {loading ? <Loading text="Carregando simulados..." /> : filtrados.length === 0 ? (
            <div className="empty-state"><h2>Nenhum simulado cadastrado.</h2>{!search && <button className="button button-secondary" onClick={() => abrirFormulario()}>Cadastrar primeiro simulado</button>}</div>
          ) : (
            <div className="table-container">
              <table>
                <thead><tr><th>Título</th><th>Tema</th><th>Perguntas</th><th className="actions-column">Ações</th></tr></thead>
                <tbody>{filtrados.map((simulado) => (
                  <tr key={simulado.id}>
                    <td><strong>{simulado.titulo}</strong></td>
                    <td>{simulado.tema}</td>
                    <td>{simulado.perguntas.length}</td>
                    <td><div className="row-actions">
                      <button className="button button-small button-light" onClick={() => abrirFormulario(simulado)}>Editar</button>
                      <button className="button button-small button-danger" onClick={() => setSelectedSimulado(simulado)}>Excluir</button>
                    </div></td>
                  </tr>
                ))}</tbody>
              </table>
            </div>
          )}
        </section>
      </section>
      {formOpen && <SimuladoModalForm simulado={editingSimulado} temas={temas} loading={actionLoading} error={error} onClearError={() => setError("")} onClose={() => !actionLoading && setFormOpen(false)} onSubmit={salvar} />}
      {selectedSimulado && <ConfirmModal title="Excluir simulado" message="Deseja realmente excluir este simulado e suas perguntas?" loading={actionLoading} onCancel={() => !actionLoading && setSelectedSimulado(null)} onConfirm={excluir} />}
    </main>
  );
}
