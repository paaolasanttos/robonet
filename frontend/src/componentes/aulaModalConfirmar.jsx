export default function ConfirmModal({ title, message, loading, onCancel, onConfirm }) {
  return (
    <div className="modal-backdrop" role="presentation">
      <div className="modal modal-small" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
        <div className="modal-header">
          <div>
            <span className="eyebrow">Confirmação</span>
            <h2 id="confirm-title">{title}</h2>
          </div>
        </div>

        <p className="confirm-message">{message}</p>

        <div className="modal-footer">
          <button className="button button-secondary" onClick={onCancel} disabled={loading}>
            Cancelar
          </button>
          <button className="button button-danger-filled" onClick={onConfirm} disabled={loading}>
            {loading ? "Apagando informação..." : "Excluir"}
          </button>
        </div>
      </div>
    </div>
  );
}