export default function Loading({ text = "Carregando..." }) {
  return (
    <div className="loading-state" role="status">
      <span className="spinner" />
      <span>{text}</span>
    </div>
  );
}