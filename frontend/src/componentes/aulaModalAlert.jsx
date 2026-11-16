export default function Alert({ type, message, onClose }) {
  return (
    <div className={`alert alert-${type}`} role="alert">
      <span>{type === "success" ? "" : "!"}</span>
      <p>{message}</p>
      <button onClick={onClose} aria-label="Fechar mensagem">×</button>
    </div>
  );
}