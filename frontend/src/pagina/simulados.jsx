import { useCallback, useEffect, useMemo, useState } from "react";
import Alert from "../componentes/aulaModalAlert";
import ConfirmModal from "../componentes/aulaModalConfirmar";
import Loading from "../componentes/aulaModalCarregando";
import Paginacao, { REGISTROS_POR_PAGINA } from "../componentes/paginacao";
import SimuladoModalForm from "../componentes/simuladoModalForm";
import { aulasApi, simuladosApi } from "../servicos/api";
import MenuPadrao from "../componentes/menuPadrao";
import RodapePadrao from "../componentes/rodapePadrao";

export default function SimuladosPagina() {
  const [simulados, setSimulados] = useState([]);
  const [temas, setTemas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");
  const [search, setSearch] = useState("");
  const [pagina, setPagina] = useState(1);
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
  const totalPaginas = Math.max(1, Math.ceil(filtrados.length / REGISTROS_POR_PAGINA));
  const paginaAtual = Math.min(pagina, totalPaginas);
  const simuladosDaPagina = filtrados.slice(
    (paginaAtual - 1) * REGISTROS_POR_PAGINA,
    paginaAtual * REGISTROS_POR_PAGINA
  );

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
      <MenuPadrao />

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
              <input id="search-simulados" type="search" placeholder="Digite título ou tema..." value={search} onChange={(event) => { setSearch(event.target.value); setPagina(1); }} />
            </div>
            <span className="result-count">{filtrados.length} {filtrados.length === 1 ? "simulado" : "simulados"}</span>
          </div>
          {loading ? <Loading text="Carregando simulados..." /> : filtrados.length === 0 ? (
            <div className="empty-state"><h2>Nenhum simulado cadastrado.</h2>{!search && <button className="button button-secondary" onClick={() => abrirFormulario()}>Cadastrar primeiro simulado</button>}</div>
          ) : (
            <div className="table-container">
              <table className="tabela-responsiva">
                <thead><tr><th>Título</th><th>Tema</th><th>Perguntas</th><th className="actions-column">Ações</th></tr></thead>
                <tbody>{simuladosDaPagina.map((simulado) => (
                  <tr key={simulado.id}>
                    <td data-label="Título"><strong>{simulado.titulo}</strong></td>
                    <td data-label="Tema">{simulado.tema}</td>
                    <td data-label="Perguntas">{simulado.perguntas.length}</td>
                    <td data-label="Ações"><div className="row-actions">
                      <button className="button button-small button-light" onClick={() => abrirFormulario(simulado)}>Editar</button>
                      <button className="button button-small button-danger" onClick={() => setSelectedSimulado(simulado)}>Excluir</button>
                    </div></td>
                  </tr>
                ))}</tbody>
              </table>
            </div>
          )}
          {!loading && filtrados.length > 0 && <Paginacao pagina={paginaAtual} total={filtrados.length} aoMudar={setPagina} />}
        </section>
      </section>
      {formOpen && <SimuladoModalForm simulado={editingSimulado} temas={temas} loading={actionLoading} error={error} onClearError={() => setError("")} onClose={() => !actionLoading && setFormOpen(false)} onSubmit={salvar} />}
      {selectedSimulado && <ConfirmModal title="Excluir simulado" message="Deseja realmente excluir este simulado e suas perguntas?" loading={actionLoading} onCancel={() => !actionLoading && setSelectedSimulado(null)} onConfirm={excluir} />}
      <RodapePadrao />
    </main>
  );
}
