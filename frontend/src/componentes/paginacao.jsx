export const REGISTROS_POR_PAGINA = 5;

export default function Paginacao({ pagina, total, aoMudar }) {
  const totalPaginas = Math.max(1, Math.ceil(total / REGISTROS_POR_PAGINA));

  if (total === 0) return null;

  return (
    <div className="auditoria-paginacao" aria-label="Paginação dos registros">
      <button type="button" className="button button-secondary button-small" onClick={() => aoMudar(pagina - 1)} disabled={pagina <= 1}>
        Anterior
      </button>
      <span>Página {pagina} de {totalPaginas}</span>
      <button type="button" className="button button-secondary button-small" onClick={() => aoMudar(pagina + 1)} disabled={pagina >= totalPaginas}>
        Próxima
      </button>
    </div>
  );
}