import { useCallback, useEffect, useMemo, useState } from "react";
import { aulasApi } from "../servicos/api";
import AulaFormModal from "../componentes/aulaModalIncluir";
import AulaViewModal from "../componentes/aulaModalVisualizar";
import ConfirmModal from "../componentes/aulaModalConfirmar";
import Alert from "../componentes/aulaModalAlert";
import Loading from "../componentes/aulaModalCarregando";

export default function AulasPagina() {
  const [aulas, setAulas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");
  const [search, setSearch] = useState("");
  const [formOpen, setFormOpen] = useState(false);
  const [viewOpen, setViewOpen] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [selectedAula, setSelectedAula] = useState(null);
  const [editingAula, setEditingAula] = useState(null);

  const loadAulas = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const data = await aulasApi.listar();
      setAulas(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar as aulas.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAulas();
  }, [loadAulas]);

  useEffect(() => {
    if (!feedback) return;
    const timer = setTimeout(() => setFeedback(""), 4000);
    return () => clearTimeout(timer);
  }, [feedback]);

  const filteredAulas = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return aulas;

    return aulas.filter((aula) =>
      [aula.titulo, aula.tema, aula.descricao]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(term)
    );
  }, [aulas, search]);

  function openCreate() {
    setEditingAula(null);
    setError("");
    setFormOpen(true);
  }

  function openEdit(aula) {
    setEditingAula(aula);
    setError("");
    setFormOpen(true);
  }

  async function handleSave(formData) {
    setActionLoading(true);
    setError("");

    try {
      if (editingAula) {
        const id = editingAula.id;
        const updated = await aulasApi.atualizar(id, formData);
        setAulas((current) =>
          current.map((aula) => (aula.id === id ? updated : aula))
        );
        setFeedback("Aula atualizada com sucesso.");
      } else {
        await aulasApi.criar(formData);
        await loadAulas();
        setFeedback("Aula cadastrada com sucesso.");
      }

      setFormOpen(false);
      setEditingAula(null);
    } catch (err) {
      setError(err.message || "Não foi possível salvar a aula.");
    } finally {
      setActionLoading(false);
    }
  }

  function openView(aula) {
    setSelectedAula(aula);
    setViewOpen(true);
  }

  function openDelete(aula) {
    setSelectedAula(aula);
    setConfirmOpen(true);
    setError("");
  }

  async function handleDelete() {
    if (!selectedAula) return;

    const id = selectedAula.id;
    setActionLoading(true);
    setError("");

    try {
      await aulasApi.excluir(id);
      setAulas((current) => current.filter((aula) => aula.id !== id));
      setFeedback("Aula excluída com sucesso.");
      setConfirmOpen(false);
      setSelectedAula(null);
    } catch (err) {
      setError(err.message || "Não foi possível excluir a aula.");
    } finally {
      setActionLoading(false);
    }
  }

  return (
    <main className="app-shell">
      <nav className="navbar" aria-label="Navegação principal">
        <a className="navbar-brand" href="/aulas" aria-label="ROBONET, página inicial">
          <span className="brand-mark">R</span>
          <span>
            <strong>ROBONET</strong>
            <small>Gestão de aulas</small>
          </span>
        </a>

        <div className="navbar-menu">
          <a className="navbar-link navbar-link-active" href="/aulas" aria-current="page">
            Aulas
          </a>
        </div>

        <div className="navbar-user">
          <div className="user-copy">
            <strong>Paola Santos</strong>
            <span>Administrador</span>
          </div>
        </div>
      </nav>

      <section className="page">
        <header className="page-header">
          <div>
            <span className="eyebrow">ROBONET</span>
            <h1>Cadastro de Aulas</h1>
          </div>
          <button className="button button-primary" onClick={openCreate}>
            + Nova Aula
          </button>
        </header>

        {feedback && <Alert type="success" message={feedback} onClose={() => setFeedback("")} />}
        {error && <Alert type="error" message={error} onClose={() => setError("")} />}

        <section className="content-card">
          <div className="toolbar">
            <div className="search-wrapper">
              <label htmlFor="search">Pesquisar aulas</label>
              <input
                id="search"
                type="search"
                placeholder="Digite título, tema, descrição..."
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
            </div>
            <span className="result-count">
              {filteredAulas.length} {filteredAulas.length === 1 ? "aula" : "aulas"}
            </span>
          </div>

          {loading ? (
            <Loading text="Carregando aulas..." />
          ) : filteredAulas.length === 0 ? (
            <div className="empty-state">
              <h2>Nenhuma aula cadastrada.</h2>
              {search && <p>Tente alterar os termos da pesquisa.</p>}
              {!search && (
                <button className="button button-secondary" onClick={openCreate}>Cadastrar primeira aula</button>
              )}
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Título</th>
                    <th>Tema</th>
                    <th>Descrição</th>
                    <th className="actions-column">Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredAulas.map((aula) => (
                    <tr key={aula.id}>
                      <td>
                        <div className="title-cell">
                          <strong>{aula.titulo}</strong>
                        </div>
                      </td>
                      <td>{aula.tema || "—"}</td>
                      <td className="description-cell">{aula.descricao || "—"}</td>
                      <td>
                        <div className="row-actions">
                          <button className="button button-small button-light" onClick={() => openView(aula)}>
                            Visualizar
                          </button>
                          <button className="button button-small button-light" onClick={() => openEdit(aula)}>
                            Editar
                          </button>
                          <button className="button button-small button-danger" onClick={() => openDelete(aula)}>
                            Excluir
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </section>

      {formOpen && (
        <AulaFormModal
          aula={editingAula}
          loading={actionLoading}
          onClose={() => !actionLoading && setFormOpen(false)}
          onSubmit={handleSave}
        />
      )}

      {viewOpen && selectedAula && (
        <AulaViewModal aula={selectedAula} onClose={() => setViewOpen(false)} />
      )}

      {confirmOpen && selectedAula && (
        <ConfirmModal
          title="Excluir aula"
          message="Deseja realmente excluir esta aula?"
          loading={actionLoading}
          onCancel={() => !actionLoading && setConfirmOpen(false)}
          onConfirm={handleDelete}
        />
      )}
    </main>
  );
}