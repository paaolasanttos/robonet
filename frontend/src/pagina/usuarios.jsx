import { useEffect, useMemo, useState } from "react";
import Alert from "../componentes/aulaModalAlert";
import Loading from "../componentes/aulaModalCarregando";
import { turmasApi, usuariosApi } from "../servicos/api";
import { useAuth } from "../context/AuthContext";

const perfis = [
  { value: "aluno", label: "Aluno" },
  { value: "professor", label: "Professor" },
  { value: "admin", label: "Administrador" },
];
const formularioInicial = { nome: "", email: "", senha: "", rgm: "", perfil: "aluno", turmaId: "", turmas: [] };

export default function UsuariosPagina() {
  const { session, logout } = useAuth();
  const [usuarios, setUsuarios] = useState([]);
  const [form, setForm] = useState(formularioInicial);
  const [editingId, setEditingId] = useState(null);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
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

  useEffect(() => { loadUsuarios(); loadTurmas(); }, []);
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
    setForm((current) => ({ ...current, [name]: value }));
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
    if (!form.nome.trim() || !form.email.trim() || (form.senha.length < 8 && (!editingId || form.senha.length > 0)) || (form.perfil === "aluno" && !form.rgm.trim())) {
      setError(form.senha.length < 8 && (!editingId || form.senha.length > 0) ? "A senha deve ter pelo menos 8 caracteres." : form.perfil === "aluno" ? "Preencha nome, e-mail e RGM." : "Preencha nome e e-mail.");
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
    setForm({ nome: usuario.nome, email: usuario.email, senha: "", rgm: usuario.rgm || "", perfil: usuario.perfil, turmaId: usuario.idTurma || "", turmas: usuario.turmas || [] });
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
        {error && <Alert type="error" message={error} onClose={() => setError("")} />}

        <section className="content-card user-create-card">
          <div className="section-heading"><span className="eyebrow">Cadastro</span><h2>{editingId ? "Editar usuário" : "Novo usuário"}</h2></div>
          <form className="user-form" onSubmit={handleCreate}>
            <div className="form-field"><label htmlFor="nome">Nome *</label><input id="nome" name="nome" value={form.nome} onChange={updateForm} placeholder="Nome completo" disabled={saving} /></div>
            <div className="form-field"><label htmlFor="email">E-mail *</label><input id="email" name="email" type="email" value={form.email} onChange={updateForm} placeholder="email@umc.br" disabled={saving} /></div>
            <div className="form-field"><label htmlFor="senha">Senha {editingId ? "(opcional)" : "*"}</label><input id="senha" name="senha" type="password" value={form.senha} onChange={updateForm} placeholder={editingId ? "Deixe vazia para manter" : "Mínimo de 8 caracteres"} disabled={saving} required={!editingId} /></div>
            <div className="form-field"><label htmlFor="perfil">Perfil *</label><select id="perfil" name="perfil" value={form.perfil} onChange={updateForm} disabled={saving}>{perfis.map((perfil) => <option key={perfil.value} value={perfil.value}>{perfil.label}</option>)}</select></div>
            {form.perfil === "aluno" && <div className="form-field"><label htmlFor="rgm">RGM *</label><input id="rgm" name="rgm" value={form.rgm} onChange={updateForm} placeholder="Número do RGM" disabled={saving} /></div>}
            {form.perfil === "aluno" && <div className="form-field"><label htmlFor="turmaId">Turma</label><select id="turmaId" name="turmaId" value={form.turmaId} onChange={updateForm} disabled={saving}><option value="">Sem turma</option>{turmas.map((turma) => <option key={turma.id} value={turma.id}>{turma.nome}</option>)}</select></div>}
            {form.perfil === "professor" && (
              <fieldset className="user-form-turmas" disabled={saving}>
                <legend>Turmas em que dá aula</legend>
                {turmas.length === 0 ? <span className="turmas-vazio">Cadastre as turmas na tela Cadastro de Turma primeiro.</span> : turmas.map((turma) => (
                  <label key={turma.id} className="checkbox-item"><input type="checkbox" checked={form.turmas.includes(turma.id)} onChange={() => toggleTurmaProfessor(turma.id)} />{turma.nome}</label>
                ))}
              </fieldset>
            )}
            <div className="user-form-actions">
              <button className="button button-primary" type="submit" disabled={saving}>{saving ? "Salvando..." : editingId ? "Salvar alterações" : "Criar usuário"}</button>
              {editingId && <button className="button button-light" type="button" onClick={cancelEditing} disabled={saving}>Cancelar edição</button>}
            </div>
          </form>
        </section>

        <section className="content-card">
          <div className="toolbar"><div className="search-wrapper"><label htmlFor="search-users">Pesquisar usuários</label><input id="search-users" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Nome, e-mail, RGM ou perfil..." /></div><span className="result-count">{filteredUsuarios.length} {filteredUsuarios.length === 1 ? "usuário" : "usuários"}</span></div>
          {loading ? <Loading text="Carregando usuários..." /> : filteredUsuarios.length === 0 ? <div className="empty-state"><h2>Nenhum usuário encontrado.</h2><p>{search ? "Tente alterar os termos da pesquisa." : "Cadastre o primeiro usuário acima."}</p></div> : (
            <div className="table-container"><table><thead><tr><th>Nome</th><th>E-mail</th><th>RGM</th><th>Turma</th><th>Perfil</th><th>Status</th><th className="actions-column">Ações</th></tr></thead><tbody>{filteredUsuarios.map((usuario) => {
              const isAdministrator = usuario.perfil?.toLowerCase() === "admin";
              return <tr key={usuario.id}><td><strong>{usuario.nome}</strong></td><td>{usuario.email}</td><td>{usuario.rgm || "-"}</td><td>{turmasDoUsuario(usuario)}</td><td><select value={usuario.perfil} disabled aria-label={`Perfil de ${usuario.nome}`}>{perfis.map((perfil) => <option key={perfil.value} value={perfil.value}>{perfil.label}</option>)}</select></td><td><span className={`status-pill ${usuario.ativo ? "status-active" : "status-inactive"}`}>{usuario.ativo ? "Ativo" : "Inativo"}</span></td><td><div className="row-actions">{!isAdministrator && <><button className="button button-small button-light" onClick={() => startEditing(usuario)}>Editar</button>{!usuario.ativo && <button className="button button-small button-light" onClick={() => handleActivateUser(usuario)}>Ativar</button>}{usuario.ativo && <button className="button button-small button-danger" onClick={() => handleDelete(usuario)}>Excluir</button>}</>}</div></td></tr>;
            })}</tbody></table></div>
          )}
        </section>
      </section>
    </main>
  );
}
