function valueOrDash(value) {
  return value || "—";
}

export default function AulaViewModal({ aula, onClose }) {
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <div className="modal" role="dialog" aria-modal="true" aria-labelledby="view-title">
        <div className="modal-header">
          <div>
            <span className="eyebrow">Detalhes</span>
            <h2 id="view-title">{valueOrDash(aula.titulo)}</h2>
          </div>
          <button className="icon-button" onClick={onClose} aria-label="Fechar">×</button>
        </div>

        <div className="details">
          <div className="detail-item">
            <span>Tema</span>
            <strong>{valueOrDash(aula.tema)}</strong>
          </div>
          <div className="detail-item">
            <span>Título</span>
            <strong>{valueOrDash(aula.titulo)}</strong>
          </div>
          <div className="detail-item detail-description">
            <span>Descrição</span>
            <p>{valueOrDash(aula.descricao)}</p>
          </div>
        </div>

        <div className="modal-footer">
          <button className="button button-primary" onClick={onClose}>Fechar</button>
        </div>
      </div>
    </div>
  );
}