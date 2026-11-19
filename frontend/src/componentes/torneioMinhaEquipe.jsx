import { useEffect, useState } from "react";
import Alert from "./aulaModalAlert";
import Loading from "./aulaModalCarregando";
import { equipesApi } from "../servicos/api";

function hoje() {
  const data = new Date();
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, "0")}-${String(data.getDate()).padStart(2, "0")}`;
}

function formatarData(data) {
  return data ? new Date(`${data}T00:00:00`).toLocaleDateString("pt-BR") : "-";
}

function aindaVaiAcontecer(torneio) {
  return torneio.status !== "Finalizado" && torneio.dtInicio >= hoje();
}

function diaSeguinte(data) {
  const proximo = new Date(`${data}T00:00:00`);
  proximo.setDate(proximo.getDate() + 1);
  return `${proximo.getFullYear()}${String(proximo.getMonth() + 1).padStart(2, "0")}${String(proximo.getDate()).padStart(2, "0")}`;
}

function linkGoogleAgenda(torneio, equipe) {
  const parametros = new URLSearchParams({
    action: "TEMPLATE",
    text: `Torneio ${torneio.nome}`,
    dates: `${torneio.dtInicio.replaceAll("-", "")}/${diaSeguinte(torneio.dtFim || torneio.dtInicio)}`,
    details: `Torneio de robótica da equipe ${equipe.nome} (${equipe.turma}) - Robonet`,
  });
  if (torneio.local) parametros.set("location", torneio.local);
  return `https://calendar.google.com/calendar/render?${parametros.toString()}`;
}

export default function TorneioMinhaEquipe() {
  const [equipe, setEquipe] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function carregar() {
      try {
        setEquipe(await equipesApi.minha());
      } catch (err) {
        setError(err.message || "Não foi possível carregar sua equipe.");
      } finally {
        setLoading(false);
      }
    }
    carregar();
  }, []);

  if (loading) return <Loading text="Carregando sua equipe..." />;

  const torneios = equipe?.torneios || [];

  return (
    <>
      {error && <Alert type="error" message={error} onClose={() => setError("")} />}

      <section className="content-card torneio-secao">
        {equipe ? (
          <>
            <div className="section-heading"><span className="eyebrow">{equipe.turma}</span><h2>{equipe.nome}</h2></div>
            <h3 className="equipe-subtitulo">Integrantes ({equipe.integrantes.length})</h3>
            <ul className="equipe-integrantes-grid">
              {equipe.integrantes.map((integrante) => (
                <li key={integrante} className="equipe-integrante-card">
                  <span className="equipe-integrante-inicial" aria-hidden="true">{integrante.charAt(0).toUpperCase()}</span>
                  <strong>{integrante}</strong>
                </li>
              ))}
            </ul>
          </>
        ) : !error && (
          <div className="empty-state">
            <h2>Você ainda não faz parte de uma equipe.</h2>
            <p>O professor da sua turma é quem monta as equipes.</p>
          </div>
        )}
      </section>

      {equipe && (
        <section className="content-card torneio-secao">
          <div className="section-heading"><span className="eyebrow">Agenda</span><h2>Torneios da equipe</h2></div>
          {torneios.length === 0 ? (
            <div className="empty-state">
              <h2>Sua equipe ainda não está inscrita em torneios.</h2>
              <p>Quando o professor inscrever a equipe, os torneios aparecerão aqui.</p>
            </div>
          ) : (
            <ul className="equipe-torneios-grid">
              {torneios.map((torneio) => {
                const futuro = aindaVaiAcontecer(torneio);
                return (
                  <li key={torneio.id} className="equipe-torneio-card">
                    <div className="equipe-torneio-topo">
                      <strong>{torneio.nome}</strong>
                      <span className={`status-pill ${torneio.status === "Finalizado" ? "status-inactive" : torneio.status === "Andamento" ? "status-pending" : "status-active"}`}>{torneio.status === "Andamento" ? "Em andamento" : torneio.status}</span>
                    </div>
                    <p><span>Data:</span> {formatarData(torneio.dtInicio)}{torneio.dtFim && torneio.dtFim !== torneio.dtInicio ? ` a ${formatarData(torneio.dtFim)}` : ""}</p>
                    <p><span>Local:</span> {torneio.local || "A definir"}</p>
                    {futuro ? (
                      <a className="button button-small button-primary" href={linkGoogleAgenda(torneio, equipe)} target="_blank" rel="noopener noreferrer">Adicionar ao Google Agenda</a>
                    ) : (
                      <small className="equipe-torneio-realizado">{torneio.status === "Finalizado" ? "Torneio realizado" : "Torneio já iniciado"}</small>
                    )}
                  </li>
                );
              })}
            </ul>
          )}
        </section>
      )}
    </>
  );
}
