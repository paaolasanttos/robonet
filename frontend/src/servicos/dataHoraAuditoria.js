export function normalizarDataHoraAuditoria(valor) {
  if (!valor) return valor;
  return /(?:Z|[+-]\d{2}:?\d{2})$/i.test(valor) ? valor : `${valor}Z`;
}

export function formatarDataHoraAuditoria(valor) {
  if (!valor) return "-";
  return new Date(normalizarDataHoraAuditoria(valor)).toLocaleString("pt-BR", {
    timeZone: "America/Sao_Paulo",
  });
}
