import { useEffect, useState } from "react";
import Alert from "./aulaModalAlert";
import { aulasAlunoApi } from "../servicos/api";

export default function AulaListaAluno() {
  const [aulas, setAulas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [selectedAula, setSelectedAula] = useState(null);
  const [activeTab, setActiveTab] = useState("disponiveis");

  const aulasDisponiveis = aulas.filter((aula) => !aula.concluida);
  const aulasConcluidas = aulas.filter((aula) => aula.concluida);
  const aulasDaAba = activeTab === "disponiveis" ? aulasDisponiveis : aulasConcluidas;

  useEffect(() => {
    carregarAulas();
  }, []);

  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(() => setMessage(""), 4000);
    return () => clearTimeout(timer);
  }, [message]);

  async function carregarAulas() {
    try {
      setLoading(true);
      const data = await aulasAlunoApi.listar();
      setAulas(Array.isArray(data) ? data : []);
      setError("");
    } catch (err) {
      setError(err.message || "Não foi possível carregar as aulas.");
    } finally {
      setLoading(false);
    }
  }

  async function concluirAula(aula) {
    try {
      await aulasAlunoApi.concluir(aula.id);
      setMessage(`Aula "${aula.titulo}" marcada como concluída.`);
      setError("");
      setSelectedAula(null);
      const data = await aulasAlunoApi.listar();
      setAulas(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível concluir a aula.");
    }
  }

  if (loading) {
    return <div className="empty-state"><h2>Carregando aulas...</h2></div>;
  }

  function renderGrid(itens) {
    if (!itens.length) {
      return (
        <section className="student-grid-section">
          <div className="empty-state"><h2>Nenhuma aula neste grupo.</h2></div>
        </section>
      );
    }

    return (
      <section className="student-grid-section">
        <div className="student-grid">
          {itens.map((aula) => (
            <article key={aula.id} className="student-aula-card">
              <div className="student-aula-top">
                <span className="student-aula-tema">{aula.tema || "Tema geral"}</span>
              </div>

              <h2>{aula.titulo || "Aula"}</h2>

              <p>
                {aula.descricao
                  ? aula.descricao.length > 140
                    ? `${aula.descricao.slice(0, 140)}...`
                    : aula.descricao
                  : "Sem conteúdo cadastrado para esta aula."}
              </p>

              <button className="button button-primary" onClick={() => setSelectedAula(aula)}>
                {aula.concluida ? "Revisar conteúdo" : "Abrir conteúdo"}
              </button>
            </article>
          ))}
        </div>
      </section>
    );
  }

  return (
    <section className="content-card student-aulas-panel">
      <header className="page-header student-page-header">
        <div>
          <span className="eyebrow">Aluno</span>
          <h1>Aulas</h1>
        </div>
      </header>

      {message && <Alert type="success" message={message} onClose={() => setMessage("")} />}
      {error && <Alert type="error" message={error} onClose={() => setError("")} />}

      <div className="student-aulas-tabs" role="tablist" aria-label="Categorias de aulas">
        <button
          id="aulas-disponiveis-tab"
          className={`student-aulas-tab${activeTab === "disponiveis" ? " student-aulas-tab-active" : ""}`}
          type="button"
          role="tab"
          aria-selected={activeTab === "disponiveis"}
          aria-controls="aulas-disponiveis-panel"
          onClick={() => setActiveTab("disponiveis")}
        >
          Aulas Disponíveis
        </button>
        <button
          id="aulas-concluidas-tab"
          className={`student-aulas-tab${activeTab === "concluidas" ? " student-aulas-tab-active" : ""}`}
          type="button"
          role="tab"
          aria-selected={activeTab === "concluidas"}
          aria-controls="aulas-concluidas-panel"
          onClick={() => setActiveTab("concluidas")}
        >
          Aulas Concluídas
        </button>
      </div>

      <section
        id={`aulas-${activeTab}-panel`}
        role="tabpanel"
        aria-labelledby={`aulas-${activeTab}-tab`}
        className="student-aulas-tab-panel"
      >
        {renderGrid(aulasDaAba)}
      </section>

      {selectedAula && (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={(event) => event.target === event.currentTarget && setSelectedAula(null)}
        >
          <div className="modal modal-student-aula" role="dialog" aria-modal="true" aria-labelledby="aula-modal-title">
            <div className="modal-header modal-header-compact">
              <div>
                <span className="eyebrow">Conteúdo da aula</span>
                <h2 id="aula-modal-title">{selectedAula.titulo || "Aula"}</h2>
              </div>
              <button className="icon-button" onClick={() => setSelectedAula(null)} aria-label="Fechar">×</button>
            </div>

            <div className="student-aula-modal-body">
              <div className="student-aula-highlight">
                <span>Tema</span>
                <strong>{selectedAula.tema || "Tema geral"}</strong>
              </div>

              <label className="student-aula-label" htmlFor="aula-conteudo">Conteúdo da aula</label>
              <textarea id="aula-conteudo" className="student-aula-textarea" readOnly value={selectedAula.descricao || ""} rows={12} />
            </div>

            <div className="modal-footer">
              <button className="button button-secondary" onClick={() => setSelectedAula(null)}>
                Fechar
              </button>
              {!selectedAula.concluida && (
                <button className="button button-primary" onClick={() => concluirAula(selectedAula)}>
                  Marcar aula como concluida
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
