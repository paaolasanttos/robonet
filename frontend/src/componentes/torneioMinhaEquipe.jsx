import { useEffect, useState } from "react";
import Alert from "./aulaModalAlert";
import Loading from "./aulaModalCarregando";
import { equipesApi } from "../servicos/api";

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

  return (
    <>
      {error && <Alert type="error" message={error} onClose={() => setError("")} />}

      <section className="content-card torneio-secao">
        {equipe ? (
          <>
            <div className="section-heading"><span className="eyebrow">{equipe.turma}</span><h2>{equipe.nome}</h2></div>
            <ul className="torneio-inscritas">
              {equipe.integrantes.map((integrante) => <li key={integrante}><strong>{integrante}</strong></li>)}
            </ul>
          </>
        ) : !error && (
          <div className="empty-state">
            <h2>Você ainda não faz parte de uma equipe.</h2>
            <p>O professor da sua turma é quem monta as equipes.</p>
          </div>
        )}
      </section>
    </>
  );
}
