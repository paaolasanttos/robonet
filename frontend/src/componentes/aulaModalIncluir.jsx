import { useEffect, useState } from "react";
import { aulasApi } from "../servicos/api";

function getInitialData(aula) {
  return {
    temaId: aula?.temaId ?? "",
    titulo: aula?.titulo ?? "",
    descricao: aula?.descricao ?? "",
  };
}

export default function AulaFormModal({ aula, loading, onClose, onSubmit }) {
  const [form, setForm] = useState(getInitialData(aula));
  const [errors, setErrors] = useState({});
  const [temas, setTemas] = useState([]);
  const [temasLoading, setTemasLoading] = useState(true);

  useEffect(() => {
    setForm(getInitialData(aula));
    setErrors({});
  }, [aula]);

  useEffect(() => {
    aulasApi.listarTemas()
      .then((data) => setTemas(Array.isArray(data) ? data : []))
      .catch(() => setTemas([]))
      .finally(() => setTemasLoading(false));
  }, []);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setErrors((current) => ({ ...current, [name]: "" }));
  }

  function validate() {
    const nextErrors = {};

    if (!form.temaId) nextErrors.temaId = "Selecione o tema.";
    if (!form.titulo.trim()) nextErrors.titulo = "Informe o título.";
    if (!form.descricao.trim()) nextErrors.descricao = "Informe a descrição.";

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  }

  function handleSubmit(event) {
    event.preventDefault();
    if (!validate()) return;

    onSubmit({
      temaId: Number(form.temaId),
      titulo: form.titulo.trim(),
      descricao: form.descricao.trim(),
    });
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <div className="modal" role="dialog" aria-modal="true" aria-labelledby="form-title">
        <div className="modal-header">
          <div>
            <span className="eyebrow">{aula ? "Alteração" : "Cadastro"}</span>
            <h2 id="form-title">{aula ? "Editar Aula" : "Nova Aula"}</h2>
          </div>
          <button className="icon-button" onClick={onClose} disabled={loading} aria-label="Fechar">
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-field">
              <label htmlFor="temaId">Tema *</label>
              <select id="temaId" name="temaId" value={form.temaId} onChange={handleChange} disabled={loading || temasLoading}>
                <option value="">{temasLoading ? "Carregando temas..." : "Selecione um tema"}</option>
                {temas.map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.tema}
                  </option>
                ))}
              </select>
              {errors.temaId && <span className="field-error">{errors.temaId}</span>}
            </div>

            <div className="form-field">
              <label htmlFor="titulo">Título *</label>
              <input id="titulo" name="titulo" value={form.titulo} onChange={handleChange} disabled={loading} />
              {errors.titulo && <span className="field-error">{errors.titulo}</span>}
            </div>

            <div className="form-field form-field-full">
              <label htmlFor="descricao">Descrição *</label>
              <textarea
                id="descricao"
                name="descricao"
                rows="5"
                value={form.descricao}
                onChange={handleChange}
                disabled={loading}
                placeholder="Descreva o conteúdo ou objetivo da aula..."
              />
              {errors.descricao && <span className="field-error">{errors.descricao}</span>}
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="button button-secondary" onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className="button button-primary" disabled={loading}>
              {loading ? "Salvando..." : aula ? "Salvar alterações" : "Cadastrar aula"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}