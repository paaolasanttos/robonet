import { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Alert from "../componentes/aulaModalAlert";
import Loading from "../componentes/aulaModalCarregando";
import SenhaRequisitos from "../componentes/senhaRequisitos";
import { MENSAGEM_SENHA, senhaValida } from "../componentes/senhaRegras";
import { formatarData } from "../componentes/torneioFormato";
import { meusDadosApi } from "../servicos/api";

const EMAIL_ATENDIMENTO = "robonet.umc@gmail.com";

function linkSolicitacao(dados) {
  const assunto = "Alteração de dados cadastrais - ROBONET";
  const corpo = [
    `Nome atual: ${dados.nome}`,
    ...(dados.rgm ? [`RGM atual: ${dados.rgm}`] : []),
    `E-mail cadastrado: ${dados.email}`,
    "",
    "Dado que precisa ser alterado:",
    "Valor correto:",
  ].join("\n");
  return `mailto:${EMAIL_ATENDIMENTO}?subject=${encodeURIComponent(assunto)}&body=${encodeURIComponent(corpo)}`;
}

export default function MeusDadosPagina() {
  const { session, login, logout } = useAuth();
  const perfil = String(session?.usuario?.perfil || "").toLowerCase();
  const [dados, setDados] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");
  const [formEmail, setFormEmail] = useState({ novoEmail: "", senhaAtual: "", codigo: "" });
  const [codigoEnviado, setCodigoEnviado] = useState(false);
  const [formSenha, setFormSenha] = useState({ senhaAtual: "", novaSenha: "", confirmacao: "" });
  const [senhaExclusao, setSenhaExclusao] = useState("");
  const [exclusaoConfirmada, setExclusaoConfirmada] = useState(false);

  useEffect(() => {
    if (perfil === "admin") return;
    async function carregar() {
      try {
        setDados(await meusDadosApi.consultar());
      } catch (err) {
        setError(err.message || "Não foi possível carregar seus dados.");
      } finally {
        setLoading(false);
      }
    }
    carregar();
  }, [perfil]);

  if (perfil === "admin") return <Navigate to="/acesso-negado" replace />;

  function alterarEmail(event) {
    const { name, value } = event.target;
    setFormEmail((atual) => ({ ...atual, [name]: name === "codigo" ? value.replace(/\D/g, "") : value }));
  }

  function alterarSenha(event) {
    const { name, value } = event.target;
    setFormSenha((atual) => ({ ...atual, [name]: value }));
  }

  async function executar(acao) {
    setSaving(true);
    setError("");
    setFeedback("");
    try {
      await acao();
    } catch (err) {
      setError(err.message || "Não foi possível concluir a operação.");
    } finally {
      setSaving(false);
      window.scrollTo(0, 0);
    }
  }

  function enviarEmail(event) {
    event.preventDefault();
    executar(async () => {
      if (!codigoEnviado) {
        await meusDadosApi.solicitarCodigoEmail({ novoEmail: formEmail.novoEmail, senhaAtual: formEmail.senhaAtual });
        setCodigoEnviado(true);
        setFeedback(`Enviamos um código para ${formEmail.novoEmail.trim()}.`);
        return;
      }
      const resposta = await meusDadosApi.confirmarEmail({ novoEmail: formEmail.novoEmail, codigo: formEmail.codigo });
      login(resposta);
      setDados((atual) => ({ ...atual, email: resposta.usuario.email }));
      cancelarEmail();
      setFeedback("E-mail alterado. Use o novo e-mail no próximo acesso.");
    });
  }

  function cancelarEmail() {
    setCodigoEnviado(false);
    setFormEmail({ novoEmail: "", senhaAtual: "", codigo: "" });
  }

  function enviarSenha(event) {
    event.preventDefault();
    if (!senhaValida(formSenha.novaSenha)) {
      setFeedback("");
      setError(MENSAGEM_SENHA);
      return;
    }
    if (formSenha.novaSenha !== formSenha.confirmacao) {
      setFeedback("");
      setError("As senhas não coincidem.");
      return;
    }
    executar(async () => {
      await meusDadosApi.alterarSenha({ senhaAtual: formSenha.senhaAtual, novaSenha: formSenha.novaSenha });
      setFormSenha({ senhaAtual: "", novaSenha: "", confirmacao: "" });
      setFeedback("Senha alterada com sucesso.");
    });
  }

  function baixarDados() {
    executar(async () => {
      const exportacao = await meusDadosApi.exportar();
      const arquivo = new Blob([JSON.stringify(exportacao, null, 2)], { type: "application/json" });
      const url = URL.createObjectURL(arquivo);
      const link = document.createElement("a");
      link.href = url;
      link.download = "robonet-meus-dados.json";
      link.click();
      URL.revokeObjectURL(url);
      setFeedback("Cópia dos seus dados gerada.");
    });
  }

  function solicitarExclusao(event) {
    event.preventDefault();
    executar(async () => {
      await meusDadosApi.solicitarExclusao({ senhaAtual: senhaExclusao });
      setDados(await meusDadosApi.consultar());
      setSenhaExclusao("");
      setExclusaoConfirmada(false);
      setFeedback("Solicitação de exclusão registrada.");
    });
  }

  function cancelarExclusao() {
    executar(async () => {
      await meusDadosApi.cancelarExclusao();
      setDados(await meusDadosApi.consultar());
      setFeedback("Solicitação de exclusão cancelada.");
    });
  }

  return (
    <main className="app-shell">
      <nav className="navbar" aria-label="Navegação principal">
        <a className="navbar-brand" href="/home" aria-label="ROBONET, página inicial">
          <span className="brand-mark">R</span>
          <span>
            <strong>ROBONET</strong>
          </span>
        </a>

        <div className="navbar-menu">
          <Link className="navbar-link" to="/home">Home</Link>
          {perfil === "aluno" ? <Link className="navbar-link" to="/aulas-aluno">Aulas</Link> : <Link className="navbar-link" to="/aulas">Aulas</Link>}
          {perfil === "aluno" ? <Link className="navbar-link" to="/simulados-aluno">Simulados</Link> : <Link className="navbar-link" to="/simulados">Simulados</Link>}
          <Link className="navbar-link" to="/torneios">Torneios</Link>
          <Link className="navbar-link navbar-link-active" to="/meus-dados" aria-current="page">Meus dados</Link>
          <Link className="navbar-link" to="/politicas">Privacidade</Link>
        </div>

        <div className="navbar-user">
          <div className="user-copy">
            <strong>{session.usuario.nome}</strong>
            <span>{session.usuario.perfil}</span>
          </div>
          <button className="logout-button" onClick={logout}>Sair</button>
        </div>
      </nav>

      <section className="page">
        <header className="page-header">
          <div>
            <span className="eyebrow">ROBONET</span>
            <h1>Meus dados</h1>
            <p>Consulte seu cadastro, altere o acesso e exerça seus direitos sobre os dados.</p>
          </div>
        </header>

        {feedback && <Alert type="success" message={feedback} onClose={() => setFeedback("")} />}
        {error && <Alert type="error" message={error} onClose={() => setError("")} />}

        {loading ? <Loading text="Carregando seus dados..." /> : dados && (
          <>
            <section className="content-card meus-dados-secao">
              <div className="section-heading"><span className="eyebrow">Cadastro</span><h2>Dados cadastrais</h2></div>
              <dl className="meus-dados-lista">
                <div><dt>Nome</dt><dd>{dados.nome}</dd></div>
                {dados.rgm && <div><dt>RGM</dt><dd>{dados.rgm}</dd></div>}
                <div><dt>E-mail</dt><dd>{dados.email}</dd></div>
                <div><dt>Perfil</dt><dd className="meus-dados-perfil">{dados.perfil}</dd></div>
                <div><dt>{dados.turmas.length > 1 ? "Turmas" : "Turma"}</dt><dd>{dados.turmas.join(", ") || "-"}</dd></div>
              </dl>
              <p className="meus-dados-ajuda">
                Para alterar os demais dados, envie um e-mail para{" "}
                <a href={linkSolicitacao(dados)}>{EMAIL_ATENDIMENTO}</a>.
              </p>
            </section>

            <section className="content-card meus-dados-secao">
              <div className="section-heading"><span className="eyebrow">Acesso</span><h2>Alterar e-mail</h2></div>
              <form className="meus-dados-form" onSubmit={enviarEmail}>
                <div className="form-field">
                  <label htmlFor="novo-email">Novo e-mail</label>
                  <input id="novo-email" name="novoEmail" type="email" value={formEmail.novoEmail} onChange={alterarEmail} autoComplete="email" required disabled={saving || codigoEnviado} />
                </div>
                {codigoEnviado ? (
                  <div className="form-field">
                    <label htmlFor="codigo-email">Código recebido</label>
                    <input id="codigo-email" name="codigo" inputMode="numeric" pattern="[0-9]{6}" maxLength="6" value={formEmail.codigo} onChange={alterarEmail} autoComplete="one-time-code" required disabled={saving} />
                  </div>
                ) : (
                  <div className="form-field">
                    <label htmlFor="senha-email">Senha atual</label>
                    <input id="senha-email" name="senhaAtual" type="password" value={formEmail.senhaAtual} onChange={alterarEmail} autoComplete="current-password" required disabled={saving} />
                  </div>
                )}
                <div className="user-form-actions">
                  {codigoEnviado && <button type="button" className="button button-secondary" onClick={cancelarEmail} disabled={saving}>Cancelar</button>}
                  <button type="submit" className="button button-primary" disabled={saving}>{codigoEnviado ? "Confirmar novo e-mail" : "Enviar código"}</button>
                </div>
              </form>
            </section>

            <section className="content-card meus-dados-secao">
              <div className="section-heading"><span className="eyebrow">Acesso</span><h2>Alterar senha</h2></div>
              <form className="meus-dados-form" onSubmit={enviarSenha}>
                <div className="form-field meus-dados-campo-inteiro">
                  <label htmlFor="senha-atual">Senha atual</label>
                  <input id="senha-atual" name="senhaAtual" type="password" value={formSenha.senhaAtual} onChange={alterarSenha} autoComplete="current-password" required disabled={saving} />
                </div>
                <div className="form-field">
                  <label htmlFor="nova-senha">Nova senha</label>
                  <input id="nova-senha" name="novaSenha" type="password" value={formSenha.novaSenha} onChange={alterarSenha} autoComplete="new-password" required disabled={saving} />
                </div>
                <div className="form-field">
                  <label htmlFor="confirmar-senha">Confirme a nova senha</label>
                  <input id="confirmar-senha" name="confirmacao" type="password" value={formSenha.confirmacao} onChange={alterarSenha} autoComplete="new-password" required disabled={saving} />
                </div>
                <div className="meus-dados-campo-inteiro">
                  <SenhaRequisitos senha={formSenha.novaSenha} />
                </div>
                <div className="user-form-actions">
                  <button type="submit" className="button button-primary" disabled={saving}>Alterar senha</button>
                </div>
              </form>
            </section>

            <section className="content-card meus-dados-secao">
              <div className="section-heading"><span className="eyebrow">LGPD</span><h2>Cópia dos meus dados</h2></div>
              <p className="meus-dados-ajuda">Baixe um arquivo com seu cadastro, turmas, equipe, aulas concluídas, simulados realizados, aceites do termo e acessos. Você pode guardar ou compartilhar esse arquivo como preferir.</p>
              <div className="meus-dados-acoes">
                <button type="button" className="button button-light" onClick={baixarDados} disabled={saving}>Baixar meus dados</button>
              </div>
            </section>

            <section className="content-card meus-dados-secao">
              <div className="section-heading"><span className="eyebrow">LGPD</span><h2>Excluir minha conta</h2></div>
              {dados.exclusaoAte ? (
                <>
                  <p className="meus-dados-ajuda">Exclusão solicitada em <strong>{formatarData(dados.exclusaoSolicitadaEm)}</strong>. Seus dados serão excluídos até <strong>{formatarData(dados.exclusaoAte)}</strong>. Até lá, você continua com acesso e pode cancelar o pedido.</p>
                  <div className="meus-dados-acoes">
                    <button type="button" className="button button-secondary" onClick={cancelarExclusao} disabled={saving}>Cancelar solicitação</button>
                  </div>
                </>
              ) : (
                <>
                  <div className="meus-dados-exclusao">
                    <div>
                      <h3>O que será excluído</h3>
                      <ul>
                        <li>Nome, RGM, e-mail e senha da conta</li>
                        <li>Vínculos com turmas e equipes</li>
                        <li>Aulas concluídas e simulados realizados</li>
                      </ul>
                    </div>
                    <div>
                      <h3>O que será mantido para auditoria</h3>
                      <ul>
                        <li>Registros de auditoria e de acesso</li>
                        <li>Aceites do termo e o pedido de exclusão</li>
                        <li>Resultados de torneios, sem identificar você</li>
                      </ul>
                    </div>
                  </div>
                  <form className="meus-dados-form" onSubmit={solicitarExclusao}>
                    <div className="form-field meus-dados-campo-inteiro">
                      <label htmlFor="senha-exclusao">Senha atual</label>
                      <input id="senha-exclusao" type="password" value={senhaExclusao} onChange={(event) => setSenhaExclusao(event.target.value)} autoComplete="current-password" required disabled={saving} />
                    </div>
                    <label className="checkbox-item meus-dados-campo-inteiro">
                      <input type="checkbox" checked={exclusaoConfirmada} onChange={(event) => setExclusaoConfirmada(event.target.checked)} disabled={saving} />
                      Entendo que, após o prazo de até 15 dias, a conta não poderá ser recuperada.
                    </label>
                    <div className="user-form-actions">
                      <button type="submit" className="button button-danger" disabled={saving || !exclusaoConfirmada}>Solicitar exclusão</button>
                    </div>
                  </form>
                </>
              )}
            </section>
          </>
        )}
      </section>
    </main>
  );
}
