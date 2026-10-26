import { useEffect, useState } from "react";
import Alert from "./aulaModalAlert";

function novaPergunta() {
  return {
    enunciado: "",
    opcoes: [
      { texto: "", correta: true },
      { texto: "", correta: false },
    ],
  };
}

export default function SimuladoModalForm({ simulado, temas, loading, error, onClose, onClearError, onSubmit }) {
  const [temaId, setTemaId] = useState("");
  const [titulo, setTitulo] = useState("");
  const [perguntas, setPerguntas] = useState([novaPergunta()]);
  const [validationError, setValidationError] = useState("");

  useEffect(() => {
    setTemaId(simulado ? String(simulado.temaId) : temas.length ? String(temas[0].id) : "");
    setTitulo(simulado?.titulo || "");
    setPerguntas(simulado?.perguntas?.map((pergunta) => ({
      enunciado: pergunta.enunciado,
      opcoes: pergunta.opcoes.map((opcao) => ({ texto: opcao.texto, correta: opcao.correta })),
    })) || [novaPergunta()]);
  }, [simulado, temas]);

  function atualizarPergunta(index, changes) {
    setPerguntas((atuais) => atuais.map((pergunta, i) => i === index ? { ...pergunta, ...changes } : pergunta));
  }

  function atualizarOpcao(perguntaIndex, opcaoIndex, changes) {
    const pergunta = perguntas[perguntaIndex];
    atualizarPergunta(perguntaIndex, {
      opcoes: pergunta.opcoes.map((opcao, i) => ({
        ...opcao,
        ...(i === opcaoIndex ? changes : {}),
        ...(changes.correta && i !== opcaoIndex ? { correta: false } : {}),
      })),
    });
  }

  async function enviar(event) {
    event.preventDefault();
    const perguntaSemCorreta = perguntas.some((pergunta) => pergunta.opcoes.filter((opcao) => opcao.correta).length !== 1);
    if (perguntaSemCorreta) {
      setValidationError("Marque exatamente uma opção correta em cada pergunta.");
      return;
    }
    setValidationError("");
    await onSubmit({
      temaId: Number(temaId),
      titulo: titulo.trim(),
      perguntas: perguntas.map((pergunta) => ({
        enunciado: pergunta.enunciado.trim(),
        opcoes: pergunta.opcoes.map((opcao) => ({ texto: opcao.texto.trim(), correta: opcao.correta })),
      })),
    });
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && !loading && onClose()}>
      <section className="modal modal-simulado-form" role="dialog" aria-modal="true" aria-labelledby="simulado-form-title">
        <div className="modal-header">
          <div>
            <span className="eyebrow">Cadastro de simulados</span>
            <h2 id="simulado-form-title">{simulado ? "Editar simulado" : "Novo simulado"}</h2>
          </div>
          <button className="icon-button" type="button" onClick={onClose} disabled={loading} aria-label="Fechar">×</button>
        </div>

        {(error || validationError) && <div className="simulado-form-alert"><Alert type="error" message={validationError || error} onClose={() => { setValidationError(""); onClearError(); }} /></div>}

        <form onSubmit={enviar}>
          <div className="form-grid">
            <div className="form-field form-field-full">
              <label htmlFor="simulado-titulo">Título</label>
              <input id="simulado-titulo" value={titulo} onChange={(event) => setTitulo(event.target.value)} maxLength={180} required />
            </div>
            <div className="form-field form-field-full">
              <label htmlFor="simulado-tema">Tema da aula</label>
              <select id="simulado-tema" value={temaId} onChange={(event) => setTemaId(event.target.value)} required>
                <option value="" disabled>Selecione um tema</option>
                {temas.map((tema) => <option key={tema.id} value={tema.id}>{tema.tema}</option>)}
              </select>
            </div>
          </div>

          <div className="simulado-questions-header">
            <h3>Perguntas</h3>
            <button className="button button-small button-light" type="button" onClick={() => setPerguntas((atuais) => [...atuais, novaPergunta()])}>
              + Adicionar pergunta
            </button>
          </div>

          {perguntas.map((pergunta, perguntaIndex) => (
            <fieldset className="simulado-question" key={perguntaIndex}>
              <legend>Pergunta {perguntaIndex + 1}</legend>
              <div className="simulado-question-heading">
                {perguntas.length > 1 && (
                  <button className="button button-small button-danger" type="button" onClick={() => setPerguntas((atuais) => atuais.filter((_, i) => i !== perguntaIndex))}>
                    Remover
                  </button>
                )}
              </div>
              <div className="form-field">
                <label htmlFor={`pergunta-${perguntaIndex}`}>Enunciado</label>
                <textarea id={`pergunta-${perguntaIndex}`} rows={2} required value={pergunta.enunciado} onChange={(event) => atualizarPergunta(perguntaIndex, { enunciado: event.target.value })} />
              </div>
              <div className="simulado-options">
                {pergunta.opcoes.map((opcao, opcaoIndex) => (
                  <div className="simulado-option-row" key={opcaoIndex}>
                    <input
                      type="radio"
                      name={`correta-${perguntaIndex}`}
                      aria-label={`Marcar opção ${opcaoIndex + 1} como correta`}
                      checked={opcao.correta}
                      onChange={() => atualizarOpcao(perguntaIndex, opcaoIndex, { correta: true })}
                    />
                    <input
                      aria-label={`Texto da opção ${opcaoIndex + 1}`}
                      placeholder={`Opção ${opcaoIndex + 1}`}
                      maxLength={500}
                      required
                      value={opcao.texto}
                      onChange={(event) => atualizarOpcao(perguntaIndex, opcaoIndex, { texto: event.target.value })}
                    />
                    {pergunta.opcoes.length > 2 && (
                      <button className="icon-button simulado-remove-option" type="button" onClick={() => atualizarPergunta(perguntaIndex, { opcoes: pergunta.opcoes.filter((_, i) => i !== opcaoIndex) })} aria-label={`Remover opção ${opcaoIndex + 1}`}>×</button>
                    )}
                  </div>
                ))}
                <button className="button button-small button-light" type="button" onClick={() => atualizarPergunta(perguntaIndex, { opcoes: [...pergunta.opcoes, { texto: "", correta: false }] })}>
                  + Adicionar opção
                </button>
                <small>Selecione o círculo ao lado da resposta correta.</small>
              </div>
            </fieldset>
          ))}

          <div className="modal-footer">
            <button className="button button-secondary" type="button" onClick={onClose} disabled={loading}>Cancelar</button>
            <button className="button button-primary" type="submit" disabled={loading || !temas.length}>
              {loading ? "Salvando..." : "Salvar simulado"}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
