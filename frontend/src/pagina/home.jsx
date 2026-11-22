import { useEffect, useState } from "react";
import Alert from "../componentes/aulaModalAlert";
import { useAuth } from "../context/AuthContext";
import { dashboardApi } from "../servicos/api";
import MenuPadrao from "../componentes/menuPadrao";
import RodapePadrao from "../componentes/rodapePadrao";

const formatarNumero = (valor) => Number(valor || 0).toLocaleString("pt-BR");

function LinhaResultado({ label, valor, total, className }) {
  const percentual = total > 0 ? (valor / total) * 100 : 0;

  return (
    <div className="dashboard-chart-row">
      <div className="dashboard-chart-label">
        <span>{label}</span>
        <strong>{formatarNumero(valor)}</strong>
      </div>
      <div
        className="dashboard-chart-track"
        role="progressbar"
        aria-label={label}
        aria-valuenow={valor}
        aria-valuemin={0}
        aria-valuemax={total}
      >
        <span className={`dashboard-chart-bar ${className}`} style={{ width: `${percentual}%` }} />
      </div>
    </div>
  );
}

export default function HomePagina() {
  const { session } = useAuth();
  const perfil = String(session?.usuario?.perfil || "").toLowerCase();
  const [resumo, setResumo] = useState(null);
  const [loadingResumo, setLoadingResumo] = useState(true);
  const [erroResumo, setErroResumo] = useState("");

  useEffect(() => {
    async function carregarResumo() {
      try {
        setResumo(await dashboardApi.resumo());
      } catch (error) {
        setErroResumo(error.message || "Não foi possível carregar os dados do dashboard.");
      } finally {
        setLoadingResumo(false);
      }
    }

    carregarResumo();
  }, []);

  const simulados = {
    total: resumo?.simuladosRealizados || 0,
    acertos: resumo?.acertos || 0,
    erros: resumo?.erros || 0,
  };
  const torneios = {
    total: resumo?.torneiosDisputados || 0,
    vitorias: resumo?.vitorias || 0,
    derrotas: resumo?.derrotas || 0,
  };
  return (
    <main className="app-shell">
      <MenuPadrao />

      <section className="page">
        <header className="page-header">
          <div>
            <span className="eyebrow">Dashboard</span>
            <h1>Bem-vindo ao ROBONET</h1>
            <p>{perfil === "aluno"
              ? "Acompanhe seu desempenho em simulados e torneios."
              : "Acompanhe o desempenho dos alunos em simulados e torneios."}</p>
          </div>
        </header>

        {erroResumo && <Alert type="error" message={erroResumo} onClose={() => setErroResumo("")} />}

        <section className="dashboard-results" aria-label="Resultados de simulados e torneios">
          <article className="content-card dashboard-panel" aria-labelledby="dashboard-simulados">
            <div className="dashboard-panel-heading">
              <div>
                <span className="eyebrow">Desempenho</span>
                <h2 id="dashboard-simulados">Simulados</h2>
                <p>Realizados, acertos e erros.</p>
              </div>
            </div>
            {loadingResumo ? (
              <div className="dashboard-loading" role="status">Carregando resultados...</div>
            ) : (
              <div className="dashboard-chart" aria-label={`${formatarNumero(simulados.total)} simulados realizados, ${formatarNumero(simulados.acertos)} acertos e ${formatarNumero(simulados.erros)} erros.`}>
                <div className="dashboard-total">
                  <strong>{formatarNumero(simulados.total)}</strong>
                  <span>simulados realizados</span>
                </div>
                <LinhaResultado label="Acertos" valor={simulados.acertos} total={simulados.acertos + simulados.erros} className="dashboard-bar-correct" />
                <LinhaResultado label="Erros" valor={simulados.erros} total={simulados.acertos + simulados.erros} className="dashboard-bar-incorrect" />
              </div>
            )}
          </article>

          <article className="content-card dashboard-panel" aria-labelledby="dashboard-torneios">
            <div className="dashboard-panel-heading">
              <div>
                <span className="eyebrow">Desempenho</span>
                <h2 id="dashboard-torneios">Torneios</h2>
                <p>Participações, vitórias e derrotas.</p>
              </div>
            </div>
            {loadingResumo ? (
              <div className="dashboard-loading" role="status">Carregando resultados...</div>
            ) : (
              <div className="dashboard-chart" aria-label={`${formatarNumero(torneios.total)} torneios participados, ${formatarNumero(torneios.vitorias)} vitórias e ${formatarNumero(torneios.derrotas)} derrotas.`}>
                <div className="dashboard-total">
                  <strong>{formatarNumero(torneios.total)}</strong>
                  <span>torneios participados</span>
                </div>
                <LinhaResultado label="Vitórias" valor={torneios.vitorias} total={torneios.vitorias + torneios.derrotas} className="dashboard-bar-wins" />
                <LinhaResultado label="Derrotas" valor={torneios.derrotas} total={torneios.vitorias + torneios.derrotas} className="dashboard-bar-incorrect" />
              </div>
            )}
          </article>
        </section>

      </section>
      <RodapePadrao />
    </main>
  );
}
