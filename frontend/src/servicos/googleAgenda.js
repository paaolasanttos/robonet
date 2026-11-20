function dataDoGoogleAgenda(data) {
  const parsed = new Date(`${data}T00:00:00Z`);
  if (!/^\d{4}-\d{2}-\d{2}$/.test(data) || Number.isNaN(parsed.getTime()) || parsed.toISOString().slice(0, 10) !== data) {
    throw new RangeError("Data inválida para criar evento no Google Agenda.");
  }
  return parsed;
}

function formatarDataGoogle(data) {
  return data.toISOString().slice(0, 10).replaceAll("-", "");
}

function diaSeguinte(data) {
  const proximo = dataDoGoogleAgenda(data);
  proximo.setUTCDate(proximo.getUTCDate() + 1);
  return formatarDataGoogle(proximo);
}

export function hoje() {
  const data = new Date();
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, "0")}-${String(data.getDate()).padStart(2, "0")}`;
}

export function formatarData(data) {
  return data ? dataDoGoogleAgenda(data).toLocaleDateString("pt-BR", { timeZone: "UTC" }) : "-";
}

export function aindaVaiAcontecer(torneio) {
  return torneio.status !== "Finalizado" && (torneio.dtFim || torneio.dtInicio) >= hoje();
}

export function linkGoogleAgenda(torneio, equipe) {
  const inicio = formatarDataGoogle(dataDoGoogleAgenda(torneio.dtInicio));
  const fim = diaSeguinte(torneio.dtFim || torneio.dtInicio);
  const parametros = new URLSearchParams({
    action: "TEMPLATE",
    text: `Torneio ${torneio.nome}`,
    dates: `${inicio}/${fim}`,
    details: `Torneio de robótica da equipe ${equipe.nome} (${equipe.turma}) - Robonet`,
  });
  if (torneio.local) parametros.set("location", torneio.local);
  return `https://calendar.google.com/calendar/render?${parametros.toString()}`;
}
