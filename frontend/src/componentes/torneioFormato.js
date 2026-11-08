export const statusTorneio = {
  Aberto: { label: "Inscrições abertas", className: "status-aberto" },
  Andamento: { label: "Em andamento", className: "status-andamento" },
  Finalizado: { label: "Finalizado", className: "status-active" },
};

export function formatarData(data) {
  if (!data) return "-";
  return new Date(`${data}T00:00:00`).toLocaleDateString("pt-BR");
}

function dataParaCampo(data) {
  const mes = String(data.getMonth() + 1).padStart(2, "0");
  const dia = String(data.getDate()).padStart(2, "0");
  return `${data.getFullYear()}-${mes}-${dia}`;
}

export function dataDeHoje() {
  return dataParaCampo(new Date());
}

export function diaSeguinte(data) {
  const proximo = new Date(`${data}T00:00:00`);
  proximo.setDate(proximo.getDate() + 1);
  return dataParaCampo(proximo);
}

export function periodoTorneio(torneio) {
  if (!torneio.dtFim || torneio.dtFim === torneio.dtInicio) return formatarData(torneio.dtInicio);
  return `${formatarData(torneio.dtInicio)} a ${formatarData(torneio.dtFim)}`;
}

export function nomeFase(fase, totalFases) {
  const faltam = totalFases - fase;
  if (faltam === 0) return "Final";
  if (faltam === 1) return "Semifinal";
  if (faltam === 2) return "Quartas de final";
  if (faltam === 3) return "Oitavas de final";
  return `${fase}ª fase`;
}
