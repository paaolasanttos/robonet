import { useEffect, useMemo, useState } from "react";
import Alert from "../componentes/aulaModalAlert";
import Loading from "../componentes/aulaModalCarregando";
import SenhaRequisitos from "../componentes/senhaRequisitos";
import { MENSAGEM_SENHA, senhaValida } from "../componentes/senhaRegras";
import { turmasApi, usuariosApi } from "../servicos/api";
import { useAuth } from "../context/AuthContext";

const perfis = [
  { value: "aluno", label: "Aluno" },
  { value: "professor", label: "Professor" },
  { value: "admin", label: "Administrador" },
];
const formularioInicial = { nome: "", email: "", senha: "", rgm: "", perfil: "aluno", acessoMFV: "N", turmaId: "", turmas: [] };

export default function UsuariosPagina() {
  const { session, logout } = useAuth();
  const [usuarios, setUsuarios] = useState([]);
  const [solicitacoes, setSolicitacoes] = useState([]);
  const [form, setForm] = useState(formularioInicial);
  const [aba, setAba] = useState("usuarios");
  const [editingId, setEditingId] = useState(null);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingSolicitacoes, setLoadingSolicitacoes] = useState(true);
  const [analisandoId, setAnalisandoId] = useState(null);
  const [solicitacaoEmAnalise, setSolicitacaoEmAnalise] = useState(null);
  const [statusAnalise, setStatusAnalise] = useState("deferir");
  const [motivoAnalise, setMotivoAnalise] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");
  const [turmas, setTurmas] = useState([]);

  async function loadTurmas() {
    try {
      const data = await turmasApi.listar();
      setTurmas(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar as turmas.");
    }
  }

  async function loadUsuarios() {
    setLoading(true);
    try {
      const data = await usuariosApi.listar();
      setUsuarios(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar os usuários.");
    } finally {
      setLoading(false);
    }
  }

  async function loadSolicitacoes() {
    setLoadingSolicitacoes(true);
    try {
      const data = await usuariosApi.listarSolicitacoes();
      setSolicitacoes(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar as solicitações.");
    } finally {
      setLoadingSolicitacoes(false);
    }
  }

  useEffect(() => { loadUsuarios(); loadTurmas(); loadSolicitacoes(); }, []);
  useEffect(() => {
    if (!feedback) return undefined;
    const timer = setTimeout(() => setFeedback(""), 4000);
    return () => clearTimeout(timer);
  }, [feedback]);

  const filteredUsuarios = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return usuarios;
    return usuarios.filter((usuario) =>
      [usuario.nome, usuario.email, usuario.rgm, usuario.perfil]
        .filter(Boolean).join(" ").toLowerCase().includes(term)
    );
  }, [usuarios, search]);

  function updateForm(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: name === "rgm" ? value.replace(/\D/g, "").slice(0, 11) : value }));
  }

  function toggleTurmaProfessor(id) {
    setForm((current) => ({
      ...current,
      turmas: current.turmas.includes(id) ? current.turmas.filter((turma) => turma !== id) : [...current.turmas, id],
    }));
  }

  function nomeTurma(id) {
    return turmas.find((turma) => turma.id === id)?.nome || "-";
  }

  function turmasDoUsuario(usuario) {
    if (usuario.perfil === "aluno") return usuario.idTurma ? nomeTurma(usuario.idTurma) : "-";
    if (usuario.perfil === "professor") return usuario.turmas?.length ? usuario.turmas.map(nomeTurma).join(", ") : "-";
    return "-";
  }

  async function handleCreate(event) {
    event.preventDefault();
    setError("");
    const senhaInvalida = (!editingId || form.senha.length > 0) && !senhaValida(form.senha);
    if (!form.nome.trim() || !form.email.trim() || senhaInvalida || (form.perfil === "aluno" && !form.rgm.trim())) {
      setError(senhaInvalida ? MENSAGEM_SENHA : form.perfil === "aluno" ? "Preencha nome, e-mail e RGM." : "Preencha nome e e-mail.");
      return;
    }
    if (form.perfil === "aluno" && form.rgm.length !== 11) {
      setError("O RGM deve ter exatamente 11 números.");
      return;
    }
    setSaving(true);
    try {
      const dados = { ...form, nome: form.nome.trim(), email: form.email.trim(), rgm: form.rgm.trim(), turmaId: form.turmaId ? Number(form.turmaId) : null };
      if (!dados.senha) delete dados.senha;
      if (editingId) {
        const usuario = await usuariosApi.atualizar(editingId, dados);
        setUsuarios((current) => current.map((item) => item.id === editingId ? usuario : item));
        setFeedback("Usuário atualizado com sucesso.");
      } else {
        const usuario = await usuariosApi.criar(dados);
        setUsuarios((current) => [...current, usuario]);
        setFeedback("Usuário criado com sucesso.");
      }
      setForm(formularioInicial);
      setEditingId(null);
    } catch (err) {
      setError(err.message || "Não foi possível criar o usuário.");
    } finally {
      setSaving(false);
    }
  }

  function startEditing(usuario) {
    setEditingId(usuario.id);
    setAba("novo");
    setForm({ nome: usuario.nome, email: usuario.email, senha: "", rgm: usuario.rgm || "", perfil: usuario.perfil, acessoMFV: usuario.acessoMFV || "N", turmaId: usuario.idTurma || "", turmas: usuario.turmas || [] });
    setError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function cancelEditing() {
    setEditingId(null);
    setForm(formularioInicial);
    setError("");
  }

  async function changeUser(id, action, successMessage) {
    setError("");
    try {
      const updated = await action();
      setUsuarios((current) => current.map((usuario) => usuario.id === id ? updated : usuario));
      setFeedback(successMessage);
    } catch (err) {
      setError(err.message || "Não foi possível atualizar o usuário.");
    }
  }

  function handleActivateUser(usuario) {
    return changeUser(usuario.id, () => usuariosApi.atualizarStatus(usuario.id, true), "Usuário ativado.");
  }

  function abrirAnalise(solicitacao) {
    setError("");
    setSolicitacaoEmAnalise(solicitacao);
    setStatusAnalise("deferir");
    setMotivoAnalise("");
  }

  async function salvarAnalise(event) {
    event.preventDefault();
    if (!solicitacaoEmAnalise) return;

    const motivo = motivoAnalise.trim();
    if (statusAnalise === "indeferir" && !motivo) {
      setError("Informe o motivo do indeferimento.");
      return;
    }

    setError("");
    setAnalisandoId(solicitacaoEmAnalise.id);
    try {
      if (statusAnalise === "deferir") {
        await usuariosApi.deferirSolicitacao(solicitacaoEmAnalise.id);
        setFeedback("Solicitação deferida e cadastro atualizado.");
      } else {
        await usuariosApi.indeferirSolicitacao(solicitacaoEmAnalise.id, motivo);
        setFeedback("Solicitação indeferida.");
      }
      setSolicitacaoEmAnalise(null);
      await Promise.all([loadSolicitacoes(), loadUsuarios()]);
    } catch (err) {
      setError(err.message || "Não foi possível analisar a solicitação.");
    } finally {
      setAnalisandoId(null);
    }
  }

  async function handleDelete(usuario) {
    if (!window.confirm(`Excluir ${usuario.nome}?`)) return;
    setError("");
    try {
      await usuariosApi.excluir(usuario.id);
      setUsuarios((current) => current.map((item) => item.id === usuario.id ? { ...item, ativo: false } : item));
      setFeedback("Usuário desativado com sucesso.");
    } catch (err) {
      setError(err.message || "Não foi possível excluir o usuário.");
    }
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
          <a className="navbar-link" href="/home">Home</a>
          <a className="navbar-link" href="/aulas">Aulas</a>
          <a className="navbar-link" href="/simulados">Simulados</a>
          <a className="navbar-link" href="/torneios">Torneios</a>
          <a className="navbar-link" href="/turmas">Turmas</a>
          <a className="navbar-link navbar-link-active" href="/usuarios" aria-current="page">Usuários</a>
          <a className="navbar-link" href="/auditoria">Auditoria</a>
          <a className="navbar-link" href="/politicas">Privacidade</a>
        </div>
        <div className="navbar-user"><div className="user-copy"><strong>{session.usuario.nome}</strong><span>{session.usuario.perfil}</span></div><button className="logout-button" onClick={logout}>Sair</button></div>
      </nav>

      <section className="page">
        <header className="page-header"><div><span className="eyebrow">ROBONET</span><h1>Administração de usuários</h1><p>Controle os perfis e o acesso à plataforma.</p></div></header>
        {feedback && <Alert type="success" message={feedback} onClose={() => setFeedback("")} />}
        {error && !solicitacaoEmAnalise && <Alert type="error" message={error} onClose={() => setError("")} />}

        <div className="abas" role="tablist" aria-label="Opções de usuários">
          <button type="button" className={`aba ${aba === "usuarios" ? "aba-ativa" : ""}`} role="tab" id="aba-usuarios" aria-controls="painel-usuarios" aria-selected={aba === "usuarios"} onClick={() => setAba("usuarios")}>Usuários Cadastrados</button>
          <button type="button" className={`aba ${aba === "novo" ? "aba-ativa" : ""}`} role="tab" id="aba-novo-usuario" aria-controls="painel-novo-usuario" aria-selected={aba === "novo"} onClick={() => setAba("novo")}>Criar novo usuário</button>
          <button type="button" className={`aba ${aba === "solicitacoes" ? "aba-ativa" : ""}`} role="tab" id="aba-solicitacoes" aria-controls="painel-solicitacoes" aria-selected={aba === "solicitacoes"} onClick={() => setAba("solicitacoes")}>Solicitações ({solicitacoes.filter((item) => item.status === "Pendente de análise").length})</button>
        </div>

        {aba === "novo" && <section className="content-card user-create-card" role="tabpanel" id="painel-novo-usuario" aria-labelledby="aba-novo-usuario" tabIndex="0">
          <div className="section-heading"><span className="eyebrow">Cadastro</span><h2>{editingId ? "Editar usuário" : "Novo usuário"}</h2></div>
          <form className="user-form" onSubmit={handleCreate}>
            <div className="form-field"><label htmlFor="nome">Nome completo *</label><input id="nome" name="nome" value={form.nome} onChange={updateForm} placeholder="Nome completo" maxLength="50" disabled={saving} /></div>
            <div className="form-field"><label htmlFor="email">E-mail *</label><input id="email" name="email" type="email" value={form.email} onChange={updateForm} placeholder="email@umc.br" disabled={saving} /></div>
            <div className="form-field"><label htmlFor="perfil">Perfil *</label><select id="perfil" name="perfil" value={form.perfil} onChange={updateForm} disabled={saving}>{perfis.map((perfil) => <option key={perfil.value} value={perfil.value}>{perfil.label}</option>)}</select></div>
            {form.perfil === "aluno" && <div className="form-field"><label htmlFor="rgm">RGM *</label><input id="rgm" name="rgm" value={form.rgm} onChange={updateForm} placeholder="11 números" inputMode="numeric" maxLength="11" disabled={saving} /></div>}
            {form.perfil === "aluno" && <div className="form-field"><label htmlFor="turmaId">Turma</label><select id="turmaId" name="turmaId" value={form.turmaId} onChange={updateForm} disabled={saving}><option value="">Sem turma</option>{turmas.map((turma) => <option key={turma.id} value={turma.id}>{turma.nome}</option>)}</select></div>}
            {form.perfil === "professor" && (
              <fieldset className="user-form-turmas" disabled={saving}>
                <legend>Turmas em que dá aula</legend>
                {turmas.length === 0 ? <span className="turmas-vazio">Cadastre as turmas na tela Cadastro de Turma primeiro.</span> : turmas.map((turma) => (
                  <label key={turma.id} className="checkbox-item"><input type="checkbox" checked={form.turmas.includes(turma.id)} onChange={() => toggleTurmaProfessor(turma.id)} />{turma.nome}</label>
                ))}
              </fieldset>
            )}
            <div className="form-field"><label htmlFor="acessoMFV">Acesso de Multi Fator de Verificação *</label><select id="acessoMFV" name="acessoMFV" value={form.acessoMFV} onChange={updateForm} disabled={saving}><option value="S">Sim</option><option value="N">Não</option></select></div>
            <div className="form-field"><label htmlFor="senha">Senha {editingId ? "(opcional)" : "*"}</label><input id="senha" name="senha" type="password" value={form.senha} onChange={updateForm} placeholder={editingId ? "Deixe vazia para manter" : "Crie uma senha"} disabled={saving} required={!editingId} /></div>
            {(!editingId || form.senha) && <SenhaRequisitos senha={form.senha} />}
            <div className="user-form-actions">
              <button className="button button-primary" type="submit" disabled={saving}>{saving ? "Salvando..." : editingId ? "Salvar alterações" : "Criar usuário"}</button>
              {editingId && <button className="button button-light" type="button" onClick={cancelEditing} disabled={saving}>Cancelar edição</button>}
            </div>
          </form>
        </section>}

        {aba === "usuarios" && <section className="content-card" role="tabpanel" id="painel-usuarios" aria-labelledby="aba-usuarios" tabIndex="0">
          <div className="toolbar"><div className="search-wrapper"><label htmlFor="search-users">Pesquisar usuários</label><input id="search-users" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Nome, e-mail, RGM ou perfil..." /></div><span className="result-count">{filteredUsuarios.length} {filteredUsuarios.length === 1 ? "usuário" : "usuários"}</span></div>
          {loading ? <Loading text="Carregando usuários..." /> : filteredUsuarios.length === 0 ? <div className="empty-state"><h2>Nenhum usuário encontrado.</h2><p>{search ? "Tente alterar os termos da pesquisa." : "Cadastre o primeiro usuário acima."}</p></div> : (
            <div className="table-container"><table><thead><tr><th>Nome</th><th>E-mail</th><th>RGM</th><th>Turma</th><th>Perfil</th><th>Acesso MFV</th><th>Status</th><th className="actions-column">Ações</th></tr></thead><tbody>{filteredUsuarios.map((usuario) => {
              const isAdministrator = usuario.perfil?.toLowerCase() === "admin";
              return <tr key={usuario.id}><td><strong>{usuario.nome}</strong></td><td>{usuario.email}</td><td>{usuario.rgm || "-"}</td><td>{turmasDoUsuario(usuario)}</td><td><select value={usuario.perfil} disabled aria-label={`Perfil de ${usuario.nome}`}>{perfis.map((perfil) => <option key={perfil.value} value={perfil.value}>{perfil.label}</option>)}</select></td><td>{usuario.acessoMFV === "S" ? "Sim" : "Não"}</td><td><span className={`status-pill ${usuario.ativo ? "status-active" : "status-inactive"}`}>{usuario.ativo ? "Ativo" : "Inativo"}</span></td><td><div className="row-actions">{!isAdministrator && <><button className="button button-small button-light" onClick={() => startEditing(usuario)}>Editar</button>{!usuario.ativo && <button className="button button-small button-light" onClick={() => handleActivateUser(usuario)}>Ativar</button>}{usuario.ativo && <button className="button button-small button-danger" onClick={() => handleDelete(usuario)}>Excluir</button>}</>}</div></td></tr>;
            })}</tbody></table></div>
          )}
        </section>}

        {aba === "solicitacoes" && <section className="content-card" role="tabpanel" id="painel-solicitacoes" aria-labelledby="aba-solicitacoes" tabIndex="0">
          <div className="section-heading"><span className="eyebrow">Análise</span><h2>Solicitações de alteração</h2></div>
          {loadingSolicitacoes ? <Loading text="Carregando solicitações..." /> : solicitacoes.length === 0 ? (
            <div className="empty-state"><h2>Nenhuma solicitação encontrada.</h2><p>Quando um usuário solicitar alterações, elas aparecerão aqui.</p></div>
          ) : (
            <div className="table-container"><table><thead><tr><th>Usuário</th><th>Dados atuais</th><th>Dados solicitados</th><th>Data</th><th>Status</th><th>Motivo</th><th className="actions-column">Ações</th></tr></thead><tbody>{solicitacoes.map((solicitacao) => {
              const pendente = solicitacao.status === "Pendente de análise";
              const classeStatus = pendente ? "status-pending" : solicitacao.status === "Deferido" ? "status-active" : "status-inactive";
              const exibirDados = (nome, rgm, perfil, turma) => (
                <div className="meus-dados-solicitacao-info">
                  <strong>{nome || "-"}</strong>
                  <span>RGM: {rgm || "-"} · Perfil: {perfil || "-"} · Turma: {turma || "-"}</span>
                </div>
              );
              return <tr key={solicitacao.id}>
                <td>{solicitacao.email}</td>
                <td>{exibirDados(solicitacao.nomeAtual, solicitacao.rgmAtual, solicitacao.perfilAtual, solicitacao.turmaAtual)}</td>
                <td>{exibirDados(solicitacao.nome, solicitacao.rgm, solicitacao.perfil, solicitacao.turma)}</td>
                <td>{solicitacao.dtRegistro ? new Date(solicitacao.dtRegistro).toLocaleDateString("pt-BR") : "-"}</td>
                <td><span className={`status-pill ${classeStatus}`}>{solicitacao.status}</span></td>
                <td>{solicitacao.motivo || "-"}</td>
                <td><div className="row-actions">{pendente && (
                  <button type="button" className="button button-small button-primary" onClick={() => abrirAnalise(solicitacao)}>Analisar</button>
                )}</div></td>
              </tr>;
            })}</tbody></table></div>
          )}
        </section>}
      </section>
      {solicitacaoEmAnalise && (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={(event) => event.target === event.currentTarget && analisandoId !== solicitacaoEmAnalise.id && setSolicitacaoEmAnalise(null)}
        >
          <section className="modal solicitacao-modal" role="dialog" aria-modal="true" aria-labelledby="solicitacao-analise-titulo">
            <div className="modal-header">
              <div><span className="eyebrow">Análise</span><h2 id="solicitacao-analise-titulo">Solicitação de {solicitacaoEmAnalise.email}</h2></div>
              <button type="button" className="icon-button" onClick={() => setSolicitacaoEmAnalise(null)} disabled={analisandoId === solicitacaoEmAnalise.id} aria-label="Fechar">×</button>
            </div>
            {error && <Alert type="error" message={error} onClose={() => setError("")} />}
            <form onSubmit={salvarAnalise}>
              <div className="form-grid">
                <h3 className="solicitacao-dados-heading">Dados atuais</h3>
                <div className="form-field"><label htmlFor="solicitacao-nome-atual">Nome</label><input id="solicitacao-nome-atual" value={solicitacaoEmAnalise.nomeAtual || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="solicitacao-rgm-atual">RGM</label><input id="solicitacao-rgm-atual" value={solicitacaoEmAnalise.rgmAtual || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="solicitacao-perfil-atual">Perfil</label><input id="solicitacao-perfil-atual" value={solicitacaoEmAnalise.perfilAtual || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="solicitacao-turma-atual">Turma</label><input id="solicitacao-turma-atual" value={solicitacaoEmAnalise.turmaAtual || "-"} readOnly /></div>

                <h3 className="solicitacao-dados-heading">Dados solicitados</h3>
                <div className="form-field"><label htmlFor="solicitacao-nome">Nome</label><input id="solicitacao-nome" value={solicitacaoEmAnalise.nome || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="solicitacao-rgm">RGM</label><input id="solicitacao-rgm" value={solicitacaoEmAnalise.rgm || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="solicitacao-perfil">Perfil</label><input id="solicitacao-perfil" value={solicitacaoEmAnalise.perfil || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="solicitacao-turma">Turma</label><input id="solicitacao-turma" value={solicitacaoEmAnalise.turma || "-"} readOnly /></div>

                <div className="form-field form-field-full">
                  <label htmlFor="solicitacao-status">Status *</label>
                  <select id="solicitacao-status" value={statusAnalise} onChange={(event) => setStatusAnalise(event.target.value)} disabled={analisandoId === solicitacaoEmAnalise.id}>
                    <option value="deferir">Deferir</option>
                    <option value="indeferir">Indeferir</option>
                  </select>
                </div>
                {statusAnalise === "indeferir" && (
                  <div className="form-field form-field-full">
                    <label htmlFor="solicitacao-motivo">Motivo do indeferimento *</label>
                    <textarea id="solicitacao-motivo" rows="3" maxLength="2000" value={motivoAnalise} onChange={(event) => setMotivoAnalise(event.target.value)} disabled={analisandoId === solicitacaoEmAnalise.id} required />
                  </div>
                )}
              </div>
              <div className="modal-footer">
                <button type="button" className="button button-secondary" onClick={() => setSolicitacaoEmAnalise(null)} disabled={analisandoId === solicitacaoEmAnalise.id}>Cancelar</button>
                <button type="submit" className="button button-primary" disabled={analisandoId === solicitacaoEmAnalise.id}>
                  {analisandoId === solicitacaoEmAnalise.id ? "Salvando..." : "Salvar"}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </main>
  );
}
