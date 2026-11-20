import { useEffect, useMemo, useState } from "react";
import Alert from "../componentes/aulaModalAlert";
import Loading from "../componentes/aulaModalCarregando";
import SenhaRequisitos from "../componentes/senhaRequisitos";
import { MENSAGEM_SENHA, senhaValida } from "../componentes/senhaRegras";
import { termoApi, turmasApi, usuariosApi } from "../servicos/api";
import { useAuth } from "../context/AuthContext";
import TermoConteudo, { formatarDataTermo, useTermoAtual } from "../componentes/termoConteudo";

const perfis = [
  { value: "aluno", label: "Aluno" },
  { value: "professor", label: "Professor" },
  { value: "admin", label: "Administrador" },
];
const formularioInicial = { nome: "", email: "", senha: "", ra: "", perfil: "aluno", acessoMFV: "N", turmaId: "", turmas: [] };

function formatarData(valor) {
  return valor ? new Date(valor).toLocaleDateString("pt-BR") : "-";
}

function proximaVersao(versao) {
  const partes = (versao || "0.0").split(".").map((parte) => Number(parte) || 0);
  partes[partes.length - 1] += 1;
  return partes.join(".");
}

export default function UsuariosPagina() {
  const { session, logout } = useAuth();
  const [usuarios, setUsuarios] = useState([]);
  const [solicitacoes, setSolicitacoes] = useState([]);
  const [cadastros, setCadastros] = useState([]);
  const [exclusoes, setExclusoes] = useState([]);
  const [form, setForm] = useState(formularioInicial);
  const [aba, setAba] = useState("usuarios");
  const [editingId, setEditingId] = useState(null);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingSolicitacoes, setLoadingSolicitacoes] = useState(true);
  const [loadingCadastros, setLoadingCadastros] = useState(true);
  const [loadingExclusoes, setLoadingExclusoes] = useState(true);
  const [emailsVisiveis, setEmailsVisiveis] = useState({});
  const [abrindoId, setAbrindoId] = useState(null);
  const [analisandoId, setAnalisandoId] = useState(null);
  const [solicitacaoEmAnalise, setSolicitacaoEmAnalise] = useState(null);
  const [cadastroEmAnalise, setCadastroEmAnalise] = useState(null);
  const [statusAnalise, setStatusAnalise] = useState("deferir");
  const [motivoAnalise, setMotivoAnalise] = useState("");
  const [turmaCadastro, setTurmaCadastro] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");
  const [turmas, setTurmas] = useState([]);
  const { termo, erro: erroTermo, setTermo } = useTermoAtual();
  const [novoTermo, setNovoTermo] = useState(null);
  const [publicando, setPublicando] = useState(false);

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
      setEmailsVisiveis({});
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

  async function loadCadastros() {
    setLoadingCadastros(true);
    try {
      const data = await usuariosApi.listarCadastros();
      setCadastros(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar as solicitações de cadastro.");
    } finally {
      setLoadingCadastros(false);
    }
  }

  async function loadExclusoes() {
    setLoadingExclusoes(true);
    try {
      const data = await usuariosApi.listarExclusoes();
      setExclusoes(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar os pedidos de exclusão.");
    } finally {
      setLoadingExclusoes(false);
    }
  }

  useEffect(() => { loadUsuarios(); loadTurmas(); loadSolicitacoes(); loadCadastros(); loadExclusoes(); }, []);
  useEffect(() => {
    if (!feedback) return undefined;
    const timer = setTimeout(() => setFeedback(""), 4000);
    return () => clearTimeout(timer);
  }, [feedback]);

  const filteredUsuarios = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return usuarios;
    return usuarios.filter((usuario) =>
      [usuario.nome, emailsVisiveis[usuario.id], usuario.ra, usuario.perfil]
        .filter(Boolean).join(" ").toLowerCase().includes(term)
    );
  }, [usuarios, search, emailsVisiveis]);

  const pendentesAlteracao = solicitacoes.filter((item) => item.status === "Pendente de análise").length;
  const pendentesCadastro = cadastros.filter((item) => item.status === "Pendente").length;
  const pendentesExclusao = exclusoes.filter((item) => item.status === "Pendente").length;

  function updateForm(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: name === "ra" ? value.replace(/\D/g, "").slice(0, 11) : value }));
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

  async function alternarEmail(usuario) {
    if (emailsVisiveis[usuario.id]) {
      setEmailsVisiveis((atual) => { const copia = { ...atual }; delete copia[usuario.id]; return copia; });
      return;
    }
    setError("");
    try {
      const data = await usuariosApi.verEmail(usuario.id);
      setEmailsVisiveis((atual) => ({ ...atual, [usuario.id]: data.email }));
    } catch (err) {
      setError(err.message || "Não foi possível exibir o e-mail.");
    }
  }

  async function handleCreate(event) {
    event.preventDefault();
    setError("");
    const senhaInvalida = (!editingId || form.senha.length > 0) && !senhaValida(form.senha);
    if (!form.nome.trim() || !form.email.trim() || senhaInvalida || (form.perfil === "aluno" && !form.ra.trim())) {
      setError(senhaInvalida ? MENSAGEM_SENHA : form.perfil === "aluno" ? "Preencha nome, e-mail e RA." : "Preencha nome e e-mail.");
      return;
    }
    if (form.perfil === "aluno" && form.ra.length !== 11) {
      setError("O RA deve ter exatamente 11 números.");
      return;
    }
    setSaving(true);
    try {
      const dados = { ...form, nome: form.nome.trim(), email: form.email.trim(), ra: form.ra.trim(), turmaId: form.turmaId ? Number(form.turmaId) : null };
      if (!dados.senha) delete dados.senha;
      if (editingId) {
        await usuariosApi.atualizar(editingId, dados);
        setFeedback("Usuário atualizado com sucesso.");
      } else {
        await usuariosApi.criar(dados);
        setFeedback("Usuário criado com sucesso.");
      }
      setForm(formularioInicial);
      setEditingId(null);
      await loadUsuarios();
    } catch (err) {
      setError(err.message || "Não foi possível criar o usuário.");
    } finally {
      setSaving(false);
    }
  }

  async function startEditing(usuario) {
    setError("");
    setAbrindoId(usuario.id);
    try {
      const email = emailsVisiveis[usuario.id] || (await usuariosApi.verEmail(usuario.id)).email;
      setEditingId(usuario.id);
      setAba("novo");
      setForm({ nome: usuario.nome, email, senha: "", ra: usuario.ra || "", perfil: usuario.perfil, acessoMFV: usuario.acessoMFV || "N", turmaId: usuario.idTurma || "", turmas: usuario.turmas || [] });
      window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (err) {
      setError(err.message || "Não foi possível abrir o usuário.");
    } finally {
      setAbrindoId(null);
    }
  }

  function cancelEditing() {
    setEditingId(null);
    setForm(formularioInicial);
    setError("");
  }

  async function handleActivateUser(usuario) {
    setError("");
    try {
      await usuariosApi.atualizarStatus(usuario.id, true);
      setFeedback("Usuário ativado.");
      await loadUsuarios();
    } catch (err) {
      setError(err.message || "Não foi possível atualizar o usuário.");
    }
  }

  async function abrirAnalise(solicitacao) {
    setError("");
    setAbrindoId(`alteracao-${solicitacao.id}`);
    try {
      const detalhe = await usuariosApi.detalharSolicitacao(solicitacao.id);
      setSolicitacaoEmAnalise(detalhe);
      setStatusAnalise("deferir");
      setMotivoAnalise("");
    } catch (err) {
      setError(err.message || "Não foi possível abrir a solicitação.");
    } finally {
      setAbrindoId(null);
    }
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

  async function abrirCadastro(cadastro) {
    setError("");
    setAbrindoId(`cadastro-${cadastro.id}`);
    try {
      const detalhe = await usuariosApi.detalharCadastro(cadastro.id);
      setCadastroEmAnalise(detalhe);
      setStatusAnalise("aprovar");
      setMotivoAnalise("");
      setTurmaCadastro("");
    } catch (err) {
      setError(err.message || "Não foi possível abrir a solicitação de cadastro.");
    } finally {
      setAbrindoId(null);
    }
  }

  async function salvarCadastro(event) {
    event.preventDefault();
    if (!cadastroEmAnalise) return;
    const motivo = motivoAnalise.trim();
    if (statusAnalise === "recusar" && !motivo) {
      setError("Informe o motivo da recusa.");
      return;
    }

    setError("");
    setAnalisandoId(cadastroEmAnalise.id);
    try {
      if (statusAnalise === "aprovar") {
        await usuariosApi.aprovarCadastro(cadastroEmAnalise.id, turmaCadastro ? Number(turmaCadastro) : null);
        setFeedback("Cadastro aprovado. O usuário já pode entrar com a senha que escolheu.");
      } else {
        await usuariosApi.recusarCadastro(cadastroEmAnalise.id, motivo);
        setFeedback("Solicitação de cadastro recusada.");
      }
      setCadastroEmAnalise(null);
      await Promise.all([loadCadastros(), loadUsuarios()]);
    } catch (err) {
      setError(err.message || "Não foi possível analisar a solicitação de cadastro.");
    } finally {
      setAnalisandoId(null);
    }
  }

  async function aprovarExclusao(exclusao) {
    if (!window.confirm("Aprovar a exclusão? A conta será anonimizada e não poderá ser recuperada.")) return;
    setError("");
    setAnalisandoId(`exclusao-${exclusao.id}`);
    try {
      await usuariosApi.aprovarExclusao(exclusao.id);
      setFeedback("Exclusão aprovada e conta anonimizada.");
      await Promise.all([loadExclusoes(), loadUsuarios()]);
    } catch (err) {
      setError(err.message || "Não foi possível aprovar a exclusão.");
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

  function iniciarNovoTermo() {
    setError("");
    setNovoTermo({ versao: proximaVersao(termo?.versao), conteudo: termo?.conteudo || "", confirmado: false });
  }

  async function publicarTermo(event) {
    event.preventDefault();
    if (!novoTermo) return;
    if (!novoTermo.versao.trim() || !novoTermo.conteudo.trim()) {
      setError("Informe a versão e o texto do termo.");
      return;
    }
    if (!window.confirm(`Publicar a versão ${novoTermo.versao.trim()}? Todos os usuários precisarão aceitar o novo termo no próximo acesso.`)) return;

    setError("");
    setPublicando(true);
    try {
      const publicado = await termoApi.publicar({ versao: novoTermo.versao.trim(), conteudo: novoTermo.conteudo });
      setTermo(publicado);
      setNovoTermo(null);
      setFeedback(`Versão ${publicado.versao} publicada. Todos os usuários deverão aceitar o novo termo.`);
      await loadUsuarios();
    } catch (err) {
      setError(err.message || "Não foi possível publicar o termo.");
    } finally {
      setPublicando(false);
    }
  }

  const modalAberto = solicitacaoEmAnalise || cadastroEmAnalise;

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
          <a className="navbar-link" href="/home">Dashboard</a>
          <a className="navbar-link" href="/aulas">Aulas</a>
          <a className="navbar-link" href="/simulados">Simulados</a>
          <a className="navbar-link" href="/torneios">Torneios</a>
          <a className="navbar-link" href="/turmas">Turmas</a>
          <a className="navbar-link navbar-link-active" href="/usuarios" aria-current="page">Usuários</a>
          <a className="navbar-link" href="/auditoria">Auditoria</a>
        </div>
        <div className="navbar-user"><div className="user-copy"><strong>{session.usuario.nome}</strong><span>{session.usuario.perfil}</span></div><button className="logout-button" onClick={logout}>Sair</button></div>
      </nav>

      <section className="page">
        <header className="page-header"><div><span className="eyebrow">ROBONET</span><h1>Administração de usuários</h1><p>Controle os perfis e o acesso à plataforma.</p></div></header>
        {feedback && <Alert type="success" message={feedback} onClose={() => setFeedback("")} />}
        {error && !modalAberto && <Alert type="error" message={error} onClose={() => setError("")} />}

        <div className="abas" role="tablist" aria-label="Opções de usuários">
          <button type="button" className={`aba ${aba === "usuarios" ? "aba-ativa" : ""}`} role="tab" id="aba-usuarios" aria-controls="painel-usuarios" aria-selected={aba === "usuarios"} onClick={() => setAba("usuarios")}>Usuários Cadastrados</button>
          <button type="button" className={`aba ${aba === "novo" ? "aba-ativa" : ""}`} role="tab" id="aba-novo-usuario" aria-controls="painel-novo-usuario" aria-selected={aba === "novo"} onClick={() => setAba("novo")}>Criar novo usuário</button>
          <button type="button" className={`aba ${aba === "cadastros" ? "aba-ativa" : ""}`} role="tab" id="aba-cadastros" aria-controls="painel-cadastros" aria-selected={aba === "cadastros"} onClick={() => setAba("cadastros")}>Pedidos de cadastro ({pendentesCadastro})</button>
          <button type="button" className={`aba ${aba === "solicitacoes" ? "aba-ativa" : ""}`} role="tab" id="aba-solicitacoes" aria-controls="painel-solicitacoes" aria-selected={aba === "solicitacoes"} onClick={() => setAba("solicitacoes")}>Solicitações ({pendentesAlteracao})</button>
          <button type="button" className={`aba ${aba === "exclusoes" ? "aba-ativa" : ""}`} role="tab" id="aba-exclusoes" aria-controls="painel-exclusoes" aria-selected={aba === "exclusoes"} onClick={() => setAba("exclusoes")}>Exclusões ({pendentesExclusao})</button>
          <button type="button" className={`aba ${aba === "termo" ? "aba-ativa" : ""}`} role="tab" id="aba-termo" aria-controls="painel-termo" aria-selected={aba === "termo"} onClick={() => setAba("termo")}>Termo de Uso e Privacidade</button>
        </div>

        {aba === "novo" && <section className="content-card user-create-card" role="tabpanel" id="painel-novo-usuario" aria-labelledby="aba-novo-usuario" tabIndex="0">
          <div className="section-heading"><span className="eyebrow">Cadastro</span><h2>{editingId ? "Editar usuário" : "Novo usuário"}</h2></div>
          <form className="user-form" onSubmit={handleCreate}>
            <div className="form-field"><label htmlFor="nome">Nome completo *</label><input id="nome" name="nome" value={form.nome} onChange={updateForm} placeholder="Nome completo" maxLength="50" disabled={saving} /></div>
            <div className="form-field"><label htmlFor="email">E-mail *</label><input id="email" name="email" type="email" value={form.email} onChange={updateForm} placeholder="email@umc.br" disabled={saving} /></div>
            <div className="form-field"><label htmlFor="perfil">Perfil *</label><select id="perfil" name="perfil" value={form.perfil} onChange={updateForm} disabled={saving}>{perfis.map((perfil) => <option key={perfil.value} value={perfil.value}>{perfil.label}</option>)}</select></div>
            {form.perfil === "aluno" && <div className="form-field"><label htmlFor="ra">RA *</label><input id="ra" name="ra" value={form.ra} onChange={updateForm} placeholder="11 números" inputMode="numeric" maxLength="11" disabled={saving} /></div>}
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
          <div className="toolbar"><div className="search-wrapper"><label htmlFor="search-users">Pesquisar usuários</label><input id="search-users" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Nome, RA ou perfil..." /></div><span className="result-count">{filteredUsuarios.length} {filteredUsuarios.length === 1 ? "usuário" : "usuários"}</span></div>
          <p className="meus-dados-ajuda">O e-mail é um dado pessoal e fica oculto. Cada visualização é registrada na auditoria.</p>
          {loading ? <Loading text="Carregando usuários..." /> : filteredUsuarios.length === 0 ? <div className="empty-state"><h2>Nenhum usuário encontrado.</h2><p>{search ? "Tente alterar os termos da pesquisa." : "Cadastre o primeiro usuário acima."}</p></div> : (
            <div className="table-container"><table className="tabela-responsiva"><thead><tr><th>Nome</th><th>E-mail</th><th>RA</th><th>Turma</th><th>Perfil</th><th>Acesso MFV</th><th>Termo aceito</th><th>Status</th><th className="actions-column">Ações</th></tr></thead><tbody>{filteredUsuarios.map((usuario) => {
              const isAdministrator = usuario.perfil?.toLowerCase() === "admin";
              const emailVisivel = emailsVisiveis[usuario.id];
              return <tr key={usuario.id}>
                <td data-label="Nome"><strong>{usuario.nome}</strong></td>
                <td data-label="E-mail"><div className="dado-sensivel-celula"><span className={emailVisivel ? "" : "dado-sensivel"} aria-label={emailVisivel ? undefined : "E-mail oculto"}>{emailVisivel || usuario.email}</span><button type="button" className="button button-small button-light" onClick={() => alternarEmail(usuario)} aria-label={`${emailVisivel ? "Ocultar" : "Ver"} e-mail de ${usuario.nome}`}>{emailVisivel ? "Ocultar" : "Ver"}</button></div></td>
                <td data-label="RA">{usuario.ra || "-"}</td>
                <td data-label="Turma">{turmasDoUsuario(usuario)}</td>
                <td data-label="Perfil">{perfis.find((perfil) => perfil.value === usuario.perfil)?.label || usuario.perfil}</td>
                <td data-label="Acesso MFV">{usuario.acessoMFV === "S" ? "Sim" : "Não"}</td>
                <td data-label="Termo aceito">{usuario.termoAceito == null ? "-" : <span className={`status-pill ${usuario.termoAceito ? "status-active" : "status-pending"}`}>{usuario.termoAceito ? "Sim" : "Não"}</span>}</td>
                <td data-label="Status"><span className={`status-pill ${usuario.ativo ? "status-active" : "status-inactive"}`}>{usuario.ativo ? "Ativo" : "Inativo"}</span></td>
                <td data-label="Ações"><div className="row-actions">{!isAdministrator && <><button className="button button-small button-light" onClick={() => startEditing(usuario)} disabled={abrindoId === usuario.id}>Editar</button>{!usuario.ativo && <button className="button button-small button-light" onClick={() => handleActivateUser(usuario)}>Ativar</button>}{usuario.ativo && <button className="button button-small button-danger" onClick={() => handleDelete(usuario)}>Excluir</button>}</>}</div></td>
              </tr>;
            })}</tbody></table></div>
          )}
        </section>}

        {aba === "cadastros" && <section className="content-card" role="tabpanel" id="painel-cadastros" aria-labelledby="aba-cadastros" tabIndex="0">
          <div className="section-heading"><span className="eyebrow">Análise</span><h2>Pedidos de cadastro</h2></div>
          <p className="meus-dados-ajuda">Pedidos enviados pela opção "Não sou cadastrado" do login. Os dados aparecem somente ao clicar em Analisar, e a visualização é registrada na auditoria.</p>
          {loadingCadastros ? <Loading text="Carregando pedidos..." /> : cadastros.length === 0 ? (
            <div className="empty-state"><h2>Nenhum pedido de cadastro.</h2><p>Quando alguém pedir acesso pelo login, o pedido aparecerá aqui.</p></div>
          ) : (
            <div className="table-container"><table className="tabela-responsiva"><thead><tr><th>E-mail</th><th>Perfil</th><th>Data</th><th>Status</th><th>Motivo</th><th className="actions-column">Ações</th></tr></thead><tbody>{cadastros.map((cadastro) => {
              const pendente = cadastro.status === "Pendente";
              const classeStatus = pendente ? "status-pending" : cadastro.status === "Aprovada" ? "status-active" : "status-inactive";
              return <tr key={cadastro.id}>
                <td data-label="E-mail">{cadastro.email}</td>
                <td data-label="Perfil">{perfis.find((perfil) => perfil.value === cadastro.perfil)?.label || cadastro.perfil}</td>
                <td data-label="Data">{formatarData(cadastro.dtSolicitacao)}</td>
                <td data-label="Status"><span className={`status-pill ${classeStatus}`}>{cadastro.status}</span></td>
                <td data-label="Motivo">{cadastro.motivo || "-"}</td>
                <td data-label="Ações"><div className="row-actions">{pendente && <button type="button" className="button button-small button-primary" onClick={() => abrirCadastro(cadastro)} disabled={abrindoId === `cadastro-${cadastro.id}`}>Analisar</button>}</div></td>
              </tr>;
            })}</tbody></table></div>
          )}
        </section>}

        {aba === "solicitacoes" && <section className="content-card" role="tabpanel" id="painel-solicitacoes" aria-labelledby="aba-solicitacoes" tabIndex="0">
          <div className="section-heading"><span className="eyebrow">Análise</span><h2>Solicitações de alteração</h2></div>
          <p className="meus-dados-ajuda">Os dados atuais e os dados solicitados aparecem somente ao clicar em Analisar, e a visualização é registrada na auditoria.</p>
          {loadingSolicitacoes ? <Loading text="Carregando solicitações..." /> : solicitacoes.length === 0 ? (
            <div className="empty-state"><h2>Nenhuma solicitação encontrada.</h2><p>Quando um usuário solicitar alterações, elas aparecerão aqui.</p></div>
          ) : (
            <div className="table-container"><table className="tabela-responsiva"><thead><tr><th>Usuário</th><th>Data</th><th>Status</th><th>Motivo</th><th className="actions-column">Ações</th></tr></thead><tbody>{solicitacoes.map((solicitacao) => {
              const pendente = solicitacao.status === "Pendente de análise";
              const classeStatus = pendente ? "status-pending" : solicitacao.status === "Deferido" ? "status-active" : "status-inactive";
              return <tr key={solicitacao.id}>
                <td data-label="Usuário">{solicitacao.email}</td>
                <td data-label="Data">{formatarData(solicitacao.dtRegistro)}</td>
                <td data-label="Status"><span className={`status-pill ${classeStatus}`}>{solicitacao.status}</span></td>
                <td data-label="Motivo">{solicitacao.motivo || "-"}</td>
                <td data-label="Ações"><div className="row-actions">{pendente && (
                  <button type="button" className="button button-small button-primary" onClick={() => abrirAnalise(solicitacao)} disabled={abrindoId === `alteracao-${solicitacao.id}`}>Analisar</button>
                )}</div></td>
              </tr>;
            })}</tbody></table></div>
          )}
        </section>}

        {aba === "exclusoes" && <section className="content-card" role="tabpanel" id="painel-exclusoes" aria-labelledby="aba-exclusoes" tabIndex="0">
          <div className="section-heading"><span className="eyebrow">LGPD</span><h2>Pedidos de exclusão de conta</h2></div>
          <p className="meus-dados-ajuda">Aprove os pedidos até a data limite de análise. Pedidos sem análise até essa data são aprovados automaticamente pelo sistema, conforme a LGPD.</p>
          {loadingExclusoes ? <Loading text="Carregando pedidos..." /> : exclusoes.length === 0 ? (
            <div className="empty-state"><h2>Nenhum pedido de exclusão.</h2><p>Quando um usuário pedir a exclusão da conta em Meus dados, o pedido aparecerá aqui.</p></div>
          ) : (
            <div className="table-container"><table className="tabela-responsiva"><thead><tr><th>Usuário</th><th>Solicitado em</th><th>Data limite de análise</th><th>Status</th><th>Aprovado por</th><th className="actions-column">Ações</th></tr></thead><tbody>{exclusoes.map((exclusao) => {
              const pendente = exclusao.status === "Pendente";
              return <tr key={exclusao.id}>
                <td data-label="Usuário">{exclusao.email}</td>
                <td data-label="Solicitado em">{formatarData(exclusao.dtSolicitacao)}</td>
                <td data-label="Data limite">{formatarData(exclusao.dtLimite)}</td>
                <td data-label="Status"><span className={`status-pill ${pendente ? "status-pending" : "status-inactive"}`}>{pendente ? "Pendente" : "Concluída"}</span></td>
                <td data-label="Aprovado por">{exclusao.usrAnalise || "-"}</td>
                <td data-label="Ações"><div className="row-actions">{pendente && <button type="button" className="button button-small button-danger" onClick={() => aprovarExclusao(exclusao)} disabled={analisandoId === `exclusao-${exclusao.id}`}>{analisandoId === `exclusao-${exclusao.id}` ? "Aprovando..." : "Aprovar"}</button>}</div></td>
              </tr>;
            })}</tbody></table></div>
          )}
        </section>}

        {aba === "termo" && <section className="content-card" role="tabpanel" id="painel-termo" aria-labelledby="aba-termo" tabIndex="0">
          <div className="section-heading"><span className="eyebrow">Termo</span><h2>Termo de Uso e Privacidade</h2></div>
          {termo && <p className="meus-dados-ajuda">Versão atual {termo.versao} • {formatarDataTermo(termo.dtVersao)}</p>}
          {!novoTermo ? (
            <>
              <div className="user-form-actions termo-admin-acoes"><button type="button" className="button button-primary" onClick={iniciarNovoTermo} disabled={!termo}>Cadastrar nova versão</button></div>
              <div className="termo-box" aria-label="Conteúdo do termo de uso e aviso de privacidade"><TermoConteudo termo={termo} erro={erroTermo} /></div>
            </>
          ) : (
            <form className="termo-editor" onSubmit={publicarTermo}>
              <div className="form-field"><label htmlFor="termo-versao">Nova versão *</label><input id="termo-versao" value={novoTermo.versao} onChange={(event) => setNovoTermo((atual) => ({ ...atual, versao: event.target.value }))} placeholder="Ex.: 0.5" maxLength="11" disabled={publicando} required /></div>
              <div className="form-field">
                <label htmlFor="termo-texto">Texto do termo *</label>
                <p className="meus-dados-ajuda">Use uma linha começando com <code>#</code> para os títulos principais e <code>##</code> para as seções. Deixe uma linha em branco entre os parágrafos. Não use HTML.</p>
                <textarea id="termo-texto" rows="18" value={novoTermo.conteudo} onChange={(event) => setNovoTermo((atual) => ({ ...atual, conteudo: event.target.value }))} disabled={publicando} required />
              </div>
              <h3>Pré-visualização</h3>
              <div className="termo-box" aria-label="Pré-visualização do novo termo"><TermoConteudo termo={{ conteudo: novoTermo.conteudo }} /></div>
              <label className="checkbox-item termo-confirmacao"><input type="checkbox" checked={novoTermo.confirmado} onChange={(event) => setNovoTermo((atual) => ({ ...atual, confirmado: event.target.checked }))} disabled={publicando} />Entendo que, ao publicar, todos os usuários precisarão aceitar o novo termo para continuar usando a plataforma.</label>
              <div className="user-form-actions">
                <button type="submit" className="button button-primary" disabled={publicando || !novoTermo.confirmado}>{publicando ? "Publicando..." : "Publicar nova versão"}</button>
                <button type="button" className="button button-light" onClick={() => setNovoTermo(null)} disabled={publicando}>Cancelar</button>
              </div>
            </form>
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
                <div className="form-field"><label htmlFor="solicitacao-ra-atual">RA</label><input id="solicitacao-ra-atual" value={solicitacaoEmAnalise.raAtual || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="solicitacao-perfil-atual">Perfil</label><input id="solicitacao-perfil-atual" value={solicitacaoEmAnalise.perfilAtual || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="solicitacao-turma-atual">Turma</label><input id="solicitacao-turma-atual" value={solicitacaoEmAnalise.turmaAtual || "-"} readOnly /></div>

                <h3 className="solicitacao-dados-heading">Dados solicitados</h3>
                <div className="form-field"><label htmlFor="solicitacao-nome">Nome</label><input id="solicitacao-nome" value={solicitacaoEmAnalise.nome || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="solicitacao-ra">RA</label><input id="solicitacao-ra" value={solicitacaoEmAnalise.ra || "-"} readOnly /></div>
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

      {cadastroEmAnalise && (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={(event) => event.target === event.currentTarget && analisandoId !== cadastroEmAnalise.id && setCadastroEmAnalise(null)}
        >
          <section className="modal solicitacao-modal" role="dialog" aria-modal="true" aria-labelledby="cadastro-analise-titulo">
            <div className="modal-header">
              <div><span className="eyebrow">Pedido de cadastro</span><h2 id="cadastro-analise-titulo">{cadastroEmAnalise.nome}</h2></div>
              <button type="button" className="icon-button" onClick={() => setCadastroEmAnalise(null)} disabled={analisandoId === cadastroEmAnalise.id} aria-label="Fechar">×</button>
            </div>
            {error && <Alert type="error" message={error} onClose={() => setError("")} />}
            <form onSubmit={salvarCadastro}>
              <div className="form-grid">
                <div className="form-field"><label htmlFor="cadastro-nome">Nome</label><input id="cadastro-nome" value={cadastroEmAnalise.nome || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="cadastro-email">E-mail</label><input id="cadastro-email" value={cadastroEmAnalise.email || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="cadastro-perfil">Perfil</label><input id="cadastro-perfil" value={perfis.find((perfil) => perfil.value === cadastroEmAnalise.perfil)?.label || cadastroEmAnalise.perfil} readOnly /></div>
                <div className="form-field"><label htmlFor="cadastro-ra">RA</label><input id="cadastro-ra" value={cadastroEmAnalise.ra || "-"} readOnly /></div>
                <div className="form-field"><label htmlFor="cadastro-data">Data do pedido</label><input id="cadastro-data" value={formatarData(cadastroEmAnalise.dtSolicitacao)} readOnly /></div>
                <div className="form-field">
                  <label htmlFor="cadastro-status">Decisão *</label>
                  <select id="cadastro-status" value={statusAnalise} onChange={(event) => setStatusAnalise(event.target.value)} disabled={analisandoId === cadastroEmAnalise.id}>
                    <option value="aprovar">Aprovar</option>
                    <option value="recusar">Recusar</option>
                  </select>
                </div>
                {statusAnalise === "aprovar" && cadastroEmAnalise.perfil === "aluno" && (
                  <div className="form-field form-field-full">
                    <label htmlFor="cadastro-turma">Turma</label>
                    <select id="cadastro-turma" value={turmaCadastro} onChange={(event) => setTurmaCadastro(event.target.value)} disabled={analisandoId === cadastroEmAnalise.id}>
                      <option value="">Sem turma</option>
                      {turmas.map((turma) => <option key={turma.id} value={turma.id}>{turma.nome}</option>)}
                    </select>
                  </div>
                )}
                {statusAnalise === "recusar" && (
                  <div className="form-field form-field-full">
                    <label htmlFor="cadastro-motivo">Motivo da recusa *</label>
                    <textarea id="cadastro-motivo" rows="3" maxLength="2000" value={motivoAnalise} onChange={(event) => setMotivoAnalise(event.target.value)} disabled={analisandoId === cadastroEmAnalise.id} required />
                  </div>
                )}
              </div>
              <div className="modal-footer">
                <button type="button" className="button button-secondary" onClick={() => setCadastroEmAnalise(null)} disabled={analisandoId === cadastroEmAnalise.id}>Cancelar</button>
                <button type="submit" className="button button-primary" disabled={analisandoId === cadastroEmAnalise.id}>
                  {analisandoId === cadastroEmAnalise.id ? "Salvando..." : "Salvar"}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </main>
  );
}
