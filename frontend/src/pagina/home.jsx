import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Alert from "../componentes/aulaModalAlert";
import { useAuth } from "../context/AuthContext";
import { dashboardApi } from "../servicos/api";

export default function HomePagina() {
  const { session, logout } = useAuth();
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

  const metricas = [
    { key: "aulas", label: "Aulas concluídas", value: resumo?.aulasConcluidas || 0, className: "dashboard-bar-lessons" },
    { key: "simulados", label: "Simulados realizados", value: resumo?.simuladosRealizados || 0, className: "dashboard-bar-quizzes" },
  ];
  const maiorMetrica = Math.max(...metricas.map((metrica) => metrica.value), 1);

  const cards = perfil === "aluno"
    ? [
        { to: "/aulas-aluno", title: "Aulas para alunos", description: "Acesse as aulas disponíveis e acompanhe o conteúdo." },
      { to: "/simulados-aluno", title: "Simulados", description: "Pratique os conteúdos das aulas e confira seu resultado." },
      { to: "/torneios", title: "Torneios", description: "Acompanhe o chaveamento e os vencedores das lutas." },
    ]
    : perfil === "professor"
    ? [
        { to: "/aulas", title: "Aulas", description: "Cadastre, edite e acompanhe as aulas da plataforma." },
        { to: "/simulados", title: "Simulados", description: "Crie questões de múltipla escolha vinculadas aos temas das aulas." },
        { to: "/torneios", title: "Torneios", description: "Monte as equipes, organize os torneios e registre os vencedores." },
      ]
    : [
        { to: "/aulas", title: "Aulas", description: "Gerencie e acompanhe as aulas cadastradas." },
        { to: "/simulados", title: "Simulados", description: "Crie questões de múltipla escolha vinculadas aos temas das aulas." },
        { to: "/turmas", title: "Cadastro de Turma", description: "Cadastre e gerencie as turmas da plataforma." },
        { to: "/usuarios", title: "Usuários", description: "Cadastre e mantenha os perfis de acesso." },
        { to: "/torneios", title: "Torneios", description: "Acompanhe e administre os torneios cadastrados." },
        { to: "/auditoria", title: "Auditoria", description: "Consulte os registros do sistema." },
      ];

  return (
    <main className="app-shell">
      <nav className="navbar" aria-label="Navegação principal">
        <a className="navbar-brand" href="/home" aria-label="ROBONET, página inicial">
          <span className="brand-mark">R</span>
          <span>
            <strong>ROBONET</strong>
            <small>Dashboard</small>
          </span>
        </a>

        <div className="navbar-menu">
          <Link className="navbar-link navbar-link-active" to="/home" aria-current="page">Dashboard</Link>
          {perfil === "aluno" ? <Link className="navbar-link" to="/aulas-aluno">Aulas</Link> : <Link className="navbar-link" to="/aulas">Aulas</Link>}
          {perfil === "aluno" ? <Link className="navbar-link" to="/simulados-aluno">Simulados</Link> : <Link className="navbar-link" to="/simulados">Simulados</Link>}
          <Link className="navbar-link" to="/torneios">Torneios</Link>
          {perfil !== "aluno" && <Link className="navbar-link" to="/usuarios">Usuários</Link>}
          {perfil === "admin" && <Link className="navbar-link" to="/turmas">Turmas</Link>}
          {perfil !== "aluno" && <Link className="navbar-link" to="/auditoria">Auditoria</Link>}
          <Link className="navbar-link" to="/politicas">Privacidade</Link>
        </div>

        <div className="navbar-user">
          <div className="user-copy">
            <strong>{session?.usuario?.nome || "Usuário"}</strong>
            <span>{session?.usuario?.perfil || "perfil"}</span>
          </div>
          <button className="logout-button" onClick={logout}>Sair</button>
        </div>
      </nav>

      <section className="page">
        <header className="page-header">
          <div>
            <span className="eyebrow">Dashboard</span>
            <h1>Bem-vindo ao ROBONET</h1>
            <p>{perfil === "aluno"
              ? "Acompanhe seu progresso em aulas e simulados."
              : "Acompanhe o progresso dos alunos ativos em aulas e simulados."}</p>
          </div>
        </header>

        <section className="content-card dashboard-panel" aria-labelledby="dashboard-chart-title">
          <div className="dashboard-panel-heading">
            <div>
              <span className="eyebrow">Progresso</span>
              <h2 id="dashboard-chart-title">Aulas concluídas x simulados realizados</h2>
              <p>{perfil === "aluno"
                ? "Seus totais registrados na plataforma."
                : "Totais registrados pelos alunos ativos da plataforma."}</p>
            </div>
            {!loadingResumo && resumo && (
              <span className="dashboard-period">{perfil === "aluno" ? "Seu resumo" : "Visão geral"}</span>
            )}
          </div>

          {erroResumo && <Alert type="error" message={erroResumo} onClose={() => setErroResumo("")} />}
          {loadingResumo ? (
            <div className="dashboard-loading" role="status">Carregando dados do dashboard...</div>
          ) : resumo && (
            <div className="dashboard-chart" role="img" aria-label={`Gráfico de barras: ${metricas[0].label}: ${metricas[0].value}; ${metricas[1].label}: ${metricas[1].value}.`}>
              {metricas.map((metrica) => (
                <div className="dashboard-chart-row" key={metrica.key}>
                  <div className="dashboard-chart-label">
                    <span>{metrica.label}</span>
                    <strong>{metrica.value}</strong>
                  </div>
                  <div className="dashboard-chart-track" aria-hidden="true">
                    <span
                      className={`dashboard-chart-bar ${metrica.className}`}
                      style={{ width: `${(metrica.value / maiorMetrica) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
              <div className="dashboard-chart-legend" aria-hidden="true">
                <span><i className="dashboard-legend-lessons" />Aulas</span>
                <span><i className="dashboard-legend-quizzes" />Simulados</span>
              </div>
            </div>
          )}
        </section>

        <section className="dashboard-cards">
          {cards.map((card) => (
            <Link key={card.title} to={card.to} className="dashboard-card-link">
              <article className="content-card dashboard-card">
                <div>
                  <h2>{card.title}</h2>
                  <p>{card.description}</p>
                </div>
                <span className="button button-primary">Acessar</span>
              </article>
            </Link>
          ))}
        </section>
      </section>
    </main>
  );
}
