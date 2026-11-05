import { useState } from "react";

function getInitialData(torneio) {
  return {
    nome: torneio?.nome ?? "",
    local: torneio?.local ?? "",
    dtInicio: torneio?.dtInicio ?? "",
    dtFim: torneio?.dtFim ?? "",
    regras: torneio?.regras ?? "",
  };
}

export default function TorneioFormModal({ torneio, loading, onClose, onSubmit }) {
  const [form, setForm] = useState(getInitialData(torneio));
  const [errors, setErrors] = useState({});

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setErrors((current) => ({ ...current, [name]: "" }));
  }

  function validate() {
    const nextErrors = {};

    if (!form.nome.trim()) nextErrors.nome = "Informe o nome do torneio.";
    if (!form.dtInicio) nextErrors.dtInicio = "Informe a data de início.";
    if (form.dtInicio && form.dtFim && form.dtFim < form.dtInicio) nextErrors.dtFim = "O término não pode ser antes do início.";

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  }

  function handleSubmit(event) {
    event.preventDefault();
    if (!validate()) return;

    onSubmit({
      nome: form.nome.trim(),
      local: form.local.trim(),
      dtInicio: form.dtInicio,
      dtFim: form.dtFim || null,
      regras: form.regras.trim(),
    });
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <div className="modal" role="dialog" aria-modal="true" aria-labelledby="torneio-form-title">
        <div className="modal-header">
          <div>
            <span className="eyebrow">{torneio ? "Alteração" : "Cadastro"}</span>
            <h2 id="torneio-form-title">{torneio ? "Editar torneio" : "Novo torneio"}</h2>
          </div>
          <button className="icon-button" onClick={onClose} disabled={loading} aria-label="Fechar">
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-field form-field-full">
              <label htmlFor="torneio-nome">Nome *</label>
              <input id="torneio-nome" name="nome" value={form.nome} onChange={handleChange} disabled={loading} maxLength="180" />
              {errors.nome && <span className="field-error">{errors.nome}</span>}
            </div>

            <div className="form-field">
              <label htmlFor="torneio-inicio">Data de início *</label>
              <input id="torneio-inicio" name="dtInicio" type="date" value={form.dtInicio} onChange={handleChange} disabled={loading} />
              {errors.dtInicio && <span className="field-error">{errors.dtInicio}</span>}
            </div>

            <div className="form-field">
              <label htmlFor="torneio-fim">Data de término</label>
              <input id="torneio-fim" name="dtFim" type="date" value={form.dtFim} onChange={handleChange} disabled={loading} />
              {errors.dtFim && <span className="field-error">{errors.dtFim}</span>}
            </div>

            <div className="form-field form-field-full">
              <label htmlFor="torneio-local">Local</label>
              <input id="torneio-local" name="local" value={form.local} onChange={handleChange} disabled={loading} maxLength="180" placeholder="Ex.: Laboratório de robótica" />
            </div>

            <div className="form-field form-field-full">
              <label htmlFor="torneio-regras">Regras</label>
              <textarea
                id="torneio-regras"
                name="regras"
                rows="4"
                value={form.regras}
                onChange={handleChange}
                disabled={loading}
                placeholder="Descreva as regras das lutas..."
              />
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="button button-secondary" onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className="button button-primary" disabled={loading}>
              {loading ? "Salvando..." : torneio ? "Salvar alterações" : "Criar torneio"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
