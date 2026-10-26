import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Alert from "../componentes/aulaModalAlert";
import { simuladosAlunoApi } from "../servicos/api";

export default function SimuladosAlunoPagina() {
  const { session, logout } = useAuth();
  const [simulados, setSimulados] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");
  const [selectedSimulado, setSelectedSimulado] = useState(null);
  const [respostas, setRespostas] = useState({});
  const [resultado, setResultado] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    async function carregar() {
      try {
        const data = await simuladosAlunoApi.listar();
        setSimulados(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(err.message || "Não foi possível carregar os simulados.");
      } finally {
        setLoading(false);
      }
    }
    carregar();
  }, []);

  useEffect(() => {
    if (!feedback) return undefined;
    const timer = setTimeout(() => setFeedback(""), 5000);
    return () => clearTimeout(timer);
  }, [feedback]);

  function abrir(simulado) {
    setSelectedSimulado(simulado);
    setRespostas({});
    setResultado(null);
    setError("");
  }

  async function enviarRespostas(event) {
    event.preventDefault();
    if (!selectedSimulado || Object.keys(respostas).length !== selectedSimulado.perguntas.length) {
      setError("Responda a todas as perguntas antes de enviar.");
      return;
    }

    setEnviando(true);
    setError("");
    try {
      const resultadoObtido = await simuladosAlunoApi.responder(
        selectedSimulado.id,
        Object.entries(respostas).map(([perguntaId, opcaoId]) => ({
          perguntaId: Number(perguntaId),
          opcaoId: Number(opcaoId),
        }))
      );
      setResultado(resultadoObtido);
      setFeedback(`Simulado concluído: ${resultadoObtido.acertos} de ${resultadoObtido.total} acertos.`);
    } catch (err) {
      setError(err.message || "Não foi possível enviar suas respostas.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <main className="app-shell">
      <nav className="navbar" aria-label="Navegação principal">
        <a className="navbar-brand" href="/home" aria-label="ROBONET, página inicial"><span className="brand-mark">R</span><span><strong>ROBONET</strong><small>Aluno</small></span></a>
        <div className="navbar-menu">
          <Link className="navbar-link" to="/home">Home</Link>
          <Link className="navbar-link" to="/aulas-aluno">Aulas</Link>
          <Link className="navbar-link navbar-link-active" to="/simulados-aluno" aria-current="page">Simulados</Link>
          <Link className="navbar-link" to="/torneios">Torneios</Link>
          <Link className="navbar-link" to="/politicas">Privacidade</Link>
        </div>
        <div className="navbar-user"><div className="user-copy"><strong>{session.usuario.nome}</strong><span>{session.usuario.perfil}</span></div><button className="logout-button" onClick={logout}>Sair</button></div>
      </nav>

      <section className="page">
        <div className="content-card student-aulas-panel">
          <header className="page-header student-page-header">
            <div><span className="eyebrow">Aluno</span><h1>Simulados</h1><p>Pratique os temas das aulas disponíveis para você.</p></div>
          </header>
          {feedback && <Alert type="success" message={feedback} onClose={() => setFeedback("")} />}
          {error && !selectedSimulado && <Alert type="error" message={error} onClose={() => setError("")} />}

          {loading ? <div className="empty-state"><h2>Carregando simulados...</h2></div> : simulados.length === 0 ? (
            <div className="empty-state"><h2>Nenhum simulado disponível no momento.</h2><p>Os simulados aparecem aqui quando são vinculados aos temas das suas aulas.</p></div>
          ) : (
            <div className="student-grid">
              {simulados.map((simulado) => (
                <article className="student-aula-card" key={simulado.id}>
                  <div className="student-aula-top"><span className="student-aula-tema">{simulado.tema || "Tema geral"}</span></div>
                  <h2>{simulado.titulo}</h2>
                  <p>{simulado.perguntas.length} {simulado.perguntas.length === 1 ? "pergunta" : "perguntas"} de múltipla escolha.</p>
                  <button className="button button-primary" onClick={() => abrir(simulado)}>Iniciar simulado</button>
                </article>
              ))}
            </div>
          )}
        </div>
      </section>

      {selectedSimulado && (
        <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && !enviando && setSelectedSimulado(null)}>
          <section className="modal modal-student-simulado" role="dialog" aria-modal="true" aria-labelledby="student-simulado-title">
            <div className="modal-header modal-header-compact">
              <div><span className="eyebrow">{selectedSimulado.tema}</span><h2 id="student-simulado-title">{selectedSimulado.titulo}</h2></div>
              <button className="icon-button" onClick={() => setSelectedSimulado(null)} disabled={enviando} aria-label="Fechar">×</button>
            </div>
            <form onSubmit={enviarRespostas}>
              <div className="student-simulado-body">
                {error && <Alert type="error" message={error} onClose={() => setError("")} />}
                {resultado && (
                  <div className="simulado-result" role="status">
                    <strong>Resultado: {resultado.acertos} de {resultado.total} acertos</strong>
                    <span>{Math.round(resultado.percentual)}% de aproveitamento</span>
                  </div>
                )}
                {selectedSimulado.perguntas.map((pergunta, index) => (
                  <fieldset className="student-simulado-question" key={pergunta.id} disabled={Boolean(resultado)}>
                    <legend>{index + 1}. {pergunta.enunciado}</legend>
                    {pergunta.opcoes.map((opcao) => (
                      <label className="student-simulado-option" key={opcao.id}>
                        <input
                          type="radio"
                          name={`resposta-${pergunta.id}`}
                          value={opcao.id}
                          checked={respostas[pergunta.id] === opcao.id}
                          onChange={() => setRespostas((atuais) => ({ ...atuais, [pergunta.id]: opcao.id }))}
                        />
                        <span>{opcao.texto}</span>
                      </label>
                    ))}
                  </fieldset>
                ))}
              </div>
              <div className="modal-footer">
                <button className="button button-secondary" type="button" onClick={() => setSelectedSimulado(null)} disabled={enviando}>Fechar</button>
                {!resultado && <button className="button button-primary" type="submit" disabled={enviando}>{enviando ? "Enviando..." : "Enviar respostas"}</button>}
              </div>
            </form>
          </section>
        </div>
      )}
    </main>
  );
}
