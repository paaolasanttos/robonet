import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Alert from "../componentes/aulaModalAlert";
import Loading from "../componentes/aulaModalCarregando";
import SenhaRequisitos from "../componentes/senhaRequisitos";
import { MENSAGEM_SENHA, senhaValida } from "../componentes/senhaRegras";
import { formatarData } from "../componentes/torneioFormato";
import { meusDadosApi } from "../servicos/api";
import TermoConteudo, { formatarDataTermo, useTermoAtual } from "../componentes/termoConteudo";
import MenuPadrao from "../componentes/menuPadrao";
import RodapePadrao from "../componentes/rodapePadrao";

export default function MeusDadosPagina() {
  const { session } = useAuth();
  const perfil = String(session?.usuario?.perfil || "").toLowerCase();
  const [dados, setDados] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");
  const [formSenha, setFormSenha] = useState({ senhaAtual: "", novaSenha: "", confirmacao: "" });
  const [formAlteracao, setFormAlteracao] = useState({ nome: "", ra: "", perfil: "aluno", turma: "" });
  const [aba, setAba] = useState("cadastro");
  const [mostrarSenhas, setMostrarSenhas] = useState(false);
  const [senhaExclusao, setSenhaExclusao] = useState("");
  const [senhaMFV, setSenhaMFV] = useState("");
  const [exclusaoConfirmada, setExclusaoConfirmada] = useState(false);
  const [solicitacaoIndeferida, setSolicitacaoIndeferida] = useState(null);
  const { termo, erro: erroTermo } = useTermoAtual();

  useEffect(() => {
    if (perfil === "admin") return;
    async function carregar() {
      try {
        const resultado = await meusDadosApi.consultar();
        setDados(resultado);
        setFormAlteracao({
          nome: resultado.nome || "",
          ra: resultado.ra || "",
          perfil: String(resultado.perfil || "aluno").toLowerCase(),
          turma: (resultado.turmas || [])[0] || "",
        });
      } catch (err) {
        setError(err.message || "Não foi possível carregar seus dados.");
      } finally {
        setLoading(false);
      }
    }
    carregar();
  }, [perfil]);

  if (perfil === "admin") return <Navigate to="/acesso-negado" replace />;

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

  function alterarDadosSolicitados(event) {
    const { name, value } = event.target;
    setFormAlteracao((atual) => ({ ...atual, [name]: value }));
  }

  function solicitarAlteracaoDados(event) {
    event.preventDefault();
    executar(async () => {
      await meusDadosApi.solicitarAlteracao(formAlteracao);
      setDados(await meusDadosApi.consultar());
      setFeedback("Solicitação de alteração dos dados registrada.");
    });
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

  function alterarAcessoMFV(event) {
    event.preventDefault();
    const ativar = dados.acessoMFV !== "S";
    executar(async () => {
      await meusDadosApi.alterarAcessoMFV({ senhaAtual: senhaMFV, acessoMFV: ativar ? "S" : "N" });
      setDados(await meusDadosApi.consultar());
      setSenhaMFV("");
      setFeedback(ativar ? "Verificação no login ativada." : "Verificação no login desativada.");
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
      <MenuPadrao />

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
            <div className="abas meus-dados-abas" role="tablist" aria-label="Opções de meus dados">
              <button type="button" className={`aba ${aba === "cadastro" ? "aba-ativa" : ""}`} role="tab" id="aba-cadastro" aria-controls="painel-cadastro" aria-selected={aba === "cadastro"} onClick={() => setAba("cadastro")}>Dados Cadastrais</button>
              <button type="button" className={`aba ${aba === "senha" ? "aba-ativa" : ""}`} role="tab" id="aba-senha" aria-controls="painel-senha" aria-selected={aba === "senha"} onClick={() => setAba("senha")}>Alterar Senha</button>
              <button type="button" className={`aba ${aba === "mfv" ? "aba-ativa" : ""}`} role="tab" id="aba-mfv" aria-controls="painel-mfv" aria-selected={aba === "mfv"} onClick={() => setAba("mfv")}>Verificação no Login</button>
              <button type="button" className={`aba ${aba === "copia" ? "aba-ativa" : ""}`} role="tab" id="aba-copia" aria-controls="painel-copia" aria-selected={aba === "copia"} onClick={() => setAba("copia")}>Cópia dos Dados</button>
              <button type="button" className={`aba ${aba === "alteracao" ? "aba-ativa" : ""}`} role="tab" id="aba-alteracao" aria-controls="painel-alteracao" aria-selected={aba === "alteracao"} onClick={() => setAba("alteracao")}>Solicitar Alteração</button>
              <button type="button" className={`aba ${aba === "exclusao" ? "aba-ativa" : ""}`} role="tab" id="aba-exclusao" aria-controls="painel-exclusao" aria-selected={aba === "exclusao"} onClick={() => setAba("exclusao")}>Solicitar Exclusão</button>
              <button type="button" className={`aba ${aba === "termo" ? "aba-ativa" : ""}`} role="tab" id="aba-termo" aria-controls="painel-termo" aria-selected={aba === "termo"} onClick={() => setAba("termo")}>Termo de Uso e Privacidade</button>
            </div>

            {aba === "cadastro" && <section className="content-card meus-dados-secao" role="tabpanel" id="painel-cadastro" aria-labelledby="aba-cadastro" tabIndex="0">
              <div className="section-heading"><span className="eyebrow">Cadastro</span><h2>Dados cadastrais</h2></div>
              <dl className="meus-dados-lista">
                <div><dt>Nome</dt><dd>{dados.nome}</dd></div>
                {dados.ra && <div><dt>RA</dt><dd>{dados.ra}</dd></div>}
                <div><dt>E-mail</dt><dd>{dados.email}</dd></div>
                <div><dt>Perfil</dt><dd className="meus-dados-perfil">{dados.perfil}</dd></div>
                <div><dt>{dados.turmas.length > 1 ? "Turmas" : "Turma"}</dt><dd>{dados.turmas.join(", ") || "-"}</dd></div>
                <div><dt>Verificação no login</dt><dd>{dados.acessoMFV === "S" ? "Ativada" : "Desativada"}</dd></div>
              </dl>
            </section>}

            {aba === "alteracao" && <section className="content-card meus-dados-secao" role="tabpanel" id="painel-alteracao" aria-labelledby="aba-alteracao" tabIndex="0">
              <div className="section-heading"><span className="eyebrow">Cadastro</span><h2>Solicitar alteração dos dados</h2></div>
              <p className="meus-dados-ajuda">Confira os campos e informe os dados corretos que deseja solicitar. A equipe responsável analisará o pedido.</p>
              <form className="meus-dados-form" onSubmit={solicitarAlteracaoDados}>
                <div className="form-field">
                  <label htmlFor="solicitacao-nome">Nome</label>
                  <input id="solicitacao-nome" name="nome" value={formAlteracao.nome} onChange={alterarDadosSolicitados} maxLength="2000" required disabled={saving} />
                </div>
                <div className="form-field">
                  <label htmlFor="solicitacao-ra">RA</label>
                  <input id="solicitacao-ra" name="ra" value={formAlteracao.ra} onChange={alterarDadosSolicitados} maxLength="50" disabled={saving} />
                </div>
                <div className="form-field">
                  <label htmlFor="solicitacao-perfil">Perfil</label>
                  <select id="solicitacao-perfil" name="perfil" value={formAlteracao.perfil} onChange={alterarDadosSolicitados} required disabled={saving}>
                    <option value="aluno">Aluno</option>
                    <option value="professor">Professor</option>
                  </select>
                </div>
                <div className="form-field">
                  <label htmlFor="solicitacao-turma">Turma</label>
                  <select id="solicitacao-turma" name="turma" value={formAlteracao.turma} onChange={alterarDadosSolicitados} disabled={saving}>
                    <option value="">Sem turma</option>
                    {(dados.turmasDisponiveis || []).map((turma) => <option key={turma.id} value={turma.nome}>{turma.nome}</option>)}
                  </select>
                </div>
                <div className="user-form-actions">
                  <button type="submit" className="button button-primary" disabled={saving}>Solicitar alteração</button>
                </div>
              </form>

              <div className="meus-dados-solicitacoes">
                <h3>Solicitações realizadas</h3>
                {(dados.solicitacoesAlteracao || []).length === 0 ? (
                  <p className="meus-dados-ajuda">Você ainda não realizou solicitações de alteração.</p>
                ) : (
                  <div className="table-container">
                    <table className="tabela-responsiva">
                      <thead><tr><th>Data</th><th>Dados solicitados</th><th>Status</th><th>Detalhes</th></tr></thead>
                      <tbody>{dados.solicitacoesAlteracao.map((solicitacao) => {
                        const statusNormalizado = String(solicitacao.status || "").normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
                        const indeferida = statusNormalizado === "indeferido";
                        const classeStatus = statusNormalizado === "deferido" ? "status-active" : indeferida ? "status-inactive" : "status-pending";
                        return (
                          <tr key={solicitacao.id}>
                            <td data-label="Data">{solicitacao.dtRegistro ? formatarData(solicitacao.dtRegistro.split("T")[0]) : "-"}</td>
                            <td data-label="Dados solicitados"><div className="meus-dados-solicitacao-info"><strong>{solicitacao.nome}</strong><span>RA: {solicitacao.ra || "-"} · Perfil: {solicitacao.perfil || "-"} · Turma: {solicitacao.turma || "-"}</span></div></td>
                            <td data-label="Status"><span className={`status-pill ${classeStatus}`}>{solicitacao.status || "Pendente de análise"}</span></td>
                            <td data-label="Detalhes">{indeferida && <button type="button" className="button button-small button-light" onClick={() => setSolicitacaoIndeferida(solicitacao)}>Ver motivo</button>}</td>
                          </tr>
                        );
                      })}</tbody>
                    </table>
                  </div>
                )}
              </div>
            </section>}

            {aba === "senha" && <section className="content-card meus-dados-secao" role="tabpanel" id="painel-senha" aria-labelledby="aba-senha" tabIndex="0">
              <div className="section-heading"><span className="eyebrow">Acesso</span><h2>Alterar senha</h2></div>
              <form className="meus-dados-form" onSubmit={enviarSenha}>
                <div className="form-field">
                  <label htmlFor="senha-atual">Senha atual</label>
                  <input id="senha-atual" name="senhaAtual" type={mostrarSenhas ? "text" : "password"} value={formSenha.senhaAtual} onChange={alterarSenha} autoComplete="current-password" required disabled={saving} />
                </div>
                <div className="form-field">
                  <label htmlFor="nova-senha">Nova senha</label>
                  <input id="nova-senha" name="novaSenha" type={mostrarSenhas ? "text" : "password"} value={formSenha.novaSenha} onChange={alterarSenha} autoComplete="new-password" required disabled={saving} />
                </div>
                <div className="form-field">
                  <label htmlFor="confirmar-senha">Confirme a nova senha</label>
                  <input id="confirmar-senha" name="confirmacao" type={mostrarSenhas ? "text" : "password"} value={formSenha.confirmacao} onChange={alterarSenha} autoComplete="new-password" required disabled={saving} />
                </div>
                <label className="checkbox-item">
                  <input type="checkbox" checked={mostrarSenhas} onChange={(event) => setMostrarSenhas(event.target.checked)} disabled={saving} />
                  Mostrar senhas
                </label>
                <SenhaRequisitos senha={formSenha.novaSenha} />
                <div className="user-form-actions">
                  <button type="submit" className="button button-primary" disabled={saving}>Alterar senha</button>
                </div>
              </form>
            </section>}

            {aba === "mfv" && <section className="content-card meus-dados-secao" role="tabpanel" id="painel-mfv" aria-labelledby="aba-mfv" tabIndex="0">
              <div className="section-heading"><span className="eyebrow">Acesso</span><h2>Verificação no login</h2></div>
              <p className="meus-dados-ajuda">Com a verificação ativada, depois de informar e-mail e senha você recebe um código de 6 dígitos no e-mail cadastrado para concluir o login. Isso protege sua conta mesmo que alguém descubra sua senha.</p>
              <p className="meus-dados-ajuda">Situação atual: <span className={`status-pill ${dados.acessoMFV === "S" ? "status-active" : "status-inactive"}`}>{dados.acessoMFV === "S" ? "Ativada" : "Desativada"}</span></p>
              <form className="meus-dados-form" onSubmit={alterarAcessoMFV}>
                <div className="form-field">
                  <label htmlFor="senha-mfv">Senha atual</label>
                  <input id="senha-mfv" type="password" value={senhaMFV} onChange={(event) => setSenhaMFV(event.target.value)} autoComplete="current-password" required disabled={saving} />
                </div>
                <div className="user-form-actions">
                  <button type="submit" className={`button ${dados.acessoMFV === "S" ? "button-secondary" : "button-primary"}`} disabled={saving}>{dados.acessoMFV === "S" ? "Desativar verificação" : "Ativar verificação"}</button>
                </div>
              </form>
            </section>}

            {aba === "copia" && <section className="content-card meus-dados-secao" role="tabpanel" id="painel-copia" aria-labelledby="aba-copia" tabIndex="0">
              <div className="section-heading"><span className="eyebrow">LGPD</span><h2>Cópia dos meus dados</h2></div>
              <p className="meus-dados-ajuda">Baixe um arquivo com seu cadastro, turmas, equipe, aulas concluídas, simulados realizados com respostas e resultados, solicitações de alteração e de exclusão, aceites do termo, acessos e registros das suas ações. Você pode guardar ou compartilhar esse arquivo como preferir.</p>
              <div className="meus-dados-acoes">
                <button type="button" className="button button-light" onClick={baixarDados} disabled={saving}>Baixar meus dados</button>
              </div>
            </section>}

            {aba === "exclusao" && <section className="content-card meus-dados-secao" role="tabpanel" id="painel-exclusao" aria-labelledby="aba-exclusao" tabIndex="0">
              <div className="section-heading"><span className="eyebrow">LGPD</span><h2>Excluir minha conta</h2></div>
              {dados.exclusaoAte ? (
                <>
                  <p className="meus-dados-ajuda">Exclusão solicitada em <strong>{formatarData(dados.exclusaoSolicitadaEm)}</strong>. Data limite de análise: <strong>{formatarData(dados.exclusaoAte)}</strong>. O administrador pode aprovar o pedido antes dessa data e, se não houver análise até lá, a exclusão será aprovada automaticamente, conforme a LGPD. Até a conclusão, você continua com acesso e pode cancelar o pedido.</p>
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
                        <li>Nome, RA, e-mail e senha da conta</li>
                        <li>Vínculos com turmas e equipes</li>
                        <li>Aulas concluídas e simulados realizados</li>
                        <li>Solicitações de alteração e de cadastro e o histórico de alterações dos seus dados</li>
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
                    <div className="form-field">
                      <label htmlFor="senha-exclusao">Senha atual</label>
                      <input id="senha-exclusao" type="password" value={senhaExclusao} onChange={(event) => setSenhaExclusao(event.target.value)} autoComplete="current-password" required disabled={saving} />
                    </div>
                    <label className="checkbox-item">
                      <input type="checkbox" checked={exclusaoConfirmada} onChange={(event) => setExclusaoConfirmada(event.target.checked)} disabled={saving} />
                      Entendo que, após a aprovação do administrador ou a data limite de análise de 14 dias, a conta não poderá ser recuperada.
                    </label>
                    <div className="user-form-actions">
                      <button type="submit" className="button button-danger" disabled={saving || !exclusaoConfirmada}>Solicitar exclusão</button>
                    </div>
                  </form>
                </>
              )}
            </section>}
            
            {aba === "termo" && <section className="content-card meus-dados-secao" role="tabpanel" id="painel-termo" aria-labelledby="aba-termo" tabIndex="0">
              <div className="section-heading"><span className="eyebrow">Termo</span><h2>Termo de uso e Privacidade</h2></div>
              {termo && <p className="meus-dados-ajuda">Versão {termo.versao} • {formatarDataTermo(termo.dtVersao)}</p>}
              <div className="termo-box meus-dados-termo-box" aria-label="Conteúdo do termo de uso e aviso de privacidade"><TermoConteudo termo={termo} erro={erroTermo} /></div>
            </section>}            
          </>
        )}
      </section>
      {solicitacaoIndeferida && (
        <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && setSolicitacaoIndeferida(null)}>
          <section className="modal modal-small" role="dialog" aria-modal="true" aria-labelledby="solicitacao-indeferida-titulo">
            <div className="modal-header">
              <div><span className="eyebrow">Solicitação indeferida</span><h2 id="solicitacao-indeferida-titulo">Motivo do indeferimento</h2></div>
              <button type="button" className="icon-button" onClick={() => setSolicitacaoIndeferida(null)} aria-label="Fechar">×</button>
            </div>
            <div className="meus-dados-motivo">
              <p>{solicitacaoIndeferida.motivo || "Não foi informado um motivo para o indeferimento."}</p>
            </div>
            <div className="modal-footer">
              <button type="button" className="button button-primary" onClick={() => setSolicitacaoIndeferida(null)}>Fechar</button>
            </div>
          </section>
        </div>
      )}
      <RodapePadrao />
    </main>
  );
}
