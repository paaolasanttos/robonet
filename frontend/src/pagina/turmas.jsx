import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import Alert from "../componentes/aulaModalAlert";
import Loading from "../componentes/aulaModalCarregando";
import { useAuth } from "../context/AuthContext";
import { turmasApi, usuariosApi } from "../servicos/api";

export default function TurmasPagina() {
  const { session, logout } = useAuth();
  const [turmas, setTurmas] = useState([]);
  const [nome, setNome] = useState("");
  const [usuarios, setUsuarios] = useState([]);
  const [professores, setProfessores] = useState([]);
  const [alunos, setAlunos] = useState([]);
  const [buscaAluno, setBuscaAluno] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");

  const carregarTurmas = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const data = await turmasApi.listar();
      setTurmas(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar as turmas.");
    } finally {
      setLoading(false);
    }
  }, []);

  const carregarUsuarios = useCallback(async () => {
    try {
      const data = await usuariosApi.listar();
      setUsuarios(Array.isArray(data) ? data.filter((usuario) => usuario.ativo) : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar os usuários.");
    }
  }, []);

  useEffect(() => {
    carregarTurmas();
    carregarUsuarios();
  }, [carregarTurmas, carregarUsuarios]);

  const professoresAtivos = useMemo(
    () => usuarios.filter((usuario) => usuario.perfil === "professor").sort((a, b) => a.nome.localeCompare(b.nome)),
    [usuarios]
  );

  const alunosFiltrados = useMemo(() => {
    const term = buscaAluno.trim().toLowerCase();
    const ativos = usuarios.filter((usuario) => usuario.perfil === "aluno").sort((a, b) => a.nome.localeCompare(b.nome));
    if (!term) return ativos;
    return ativos.filter((aluno) =>
      alunos.includes(aluno.id)
      || aluno.nome.toLowerCase().includes(term)
      || String(aluno.rgm || "").includes(term)
    );
  }, [usuarios, buscaAluno, alunos]);

  function alternar(lista, setLista, id) {
    setLista(lista.includes(id) ? lista.filter((item) => item !== id) : [...lista, id]);
  }

  function nomeTurma(id) {
    return turmas.find((turma) => turma.id === id)?.nome;
  }

  useEffect(() => {
    if (!feedback) return undefined;
    const timer = setTimeout(() => setFeedback(""), 4000);
    return () => clearTimeout(timer);
  }, [feedback]);

  async function cadastrar(event) {
    event.preventDefault();
    const nomeTurma = nome.trim();
    if (!nomeTurma) {
      setError("Informe o nome da turma.");
      return;
    }

    setSaving(true);
    setError("");
    try {
      const turma = await turmasApi.criar({ nome: nomeTurma, professores, alunos });
      setTurmas((atuais) => [...atuais, turma].sort((a, b) => a.nome.localeCompare(b.nome)));
      setNome("");
      setProfessores([]);
      setAlunos([]);
      setBuscaAluno("");
      carregarUsuarios();
      setFeedback("Turma cadastrada com sucesso.");
    } catch (err) {
      setError(err.message || "Não foi possível cadastrar a turma.");
    } finally {
      setSaving(false);
    }
  }

  async function excluir(turma) {
    if (!window.confirm(`Excluir a turma ${turma.nome}?`)) return;
    setError("");
    try {
      await turmasApi.excluir(turma.id);
      setTurmas((atuais) => atuais.filter((item) => item.id !== turma.id));
      setFeedback("Turma excluída com sucesso.");
    } catch (err) {
      setError(err.message || "Não foi possível excluir a turma.");
    }
  }

  return (
    <main className="app-shell">
      <nav className="navbar" aria-label="Navegação principal">
        <Link className="navbar-brand" to="/home" aria-label="ROBONET, página inicial">
          <span className="brand-mark">R</span>
          <span><strong>ROBONET</strong></span>
        </Link>
        <div className="navbar-menu">
          <Link className="navbar-link" to="/home">Dashboard</Link>
          <Link className="navbar-link" to="/aulas">Aulas</Link>
          <Link className="navbar-link" to="/simulados">Simulados</Link>
          <Link className="navbar-link" to="/torneios">Torneios</Link>
          <Link className="navbar-link navbar-link-active" to="/turmas" aria-current="page">Turmas</Link>
          <Link className="navbar-link" to="/usuarios">Usuários</Link>
          <Link className="navbar-link" to="/auditoria">Auditoria</Link>
        </div>
        <div className="navbar-user">
          <div className="user-copy"><strong>{session.usuario.nome}</strong><span>{session.usuario.perfil}</span></div>
          <button className="logout-button" onClick={logout}>Sair</button>
        </div>
      </nav>

      <section className="page">
        <header className="page-header">
          <div><span className="eyebrow">ROBONET</span><h1>Cadastro de Turma</h1><p>Cadastre e gerencie as turmas da plataforma.</p></div>
        </header>
        {feedback && <Alert type="success" message={feedback} onClose={() => setFeedback("")} />}
        {error && <Alert type="error" message={error} onClose={() => setError("")} />}

        <section className="content-card user-create-card">
          <div className="section-heading"><span className="eyebrow">Cadastro</span><h2>Nova turma</h2></div>
          <form className="equipe-form" onSubmit={cadastrar}>
            <div className="form-field">
              <label htmlFor="nova-turma">Nome da turma</label>
              <input id="nova-turma" value={nome} onChange={(event) => setNome(event.target.value)} placeholder="Ex.: Robótica 1º semestre" maxLength={120} disabled={saving} required />
            </div>

            <fieldset className="user-form-turmas" disabled={saving}>
              <legend>Professores ({professores.length})</legend>
              {professoresAtivos.length === 0 ? <span className="turmas-vazio">Nenhum professor ativo cadastrado.</span>
                : professoresAtivos.map((professor) => (
                  <label key={professor.id} className="checkbox-item">
                    <input type="checkbox" checked={professores.includes(professor.id)} onChange={() => alternar(professores, setProfessores, professor.id)} />
                    {professor.nome}
                  </label>
                ))}
            </fieldset>

            <div className="form-field equipe-busca">
              <label htmlFor="turma-busca-aluno">Pesquisar aluno</label>
              <input id="turma-busca-aluno" type="search" value={buscaAluno} onChange={(event) => setBuscaAluno(event.target.value)} placeholder="Nome ou RGM do aluno" disabled={saving} />
            </div>

            <fieldset className="user-form-turmas" disabled={saving}>
              <legend>Alunos ({alunos.length})</legend>
              {alunosFiltrados.length === 0 ? <span className="turmas-vazio">{buscaAluno.trim() ? "Nenhum aluno encontrado com esse nome ou RGM." : "Nenhum aluno ativo cadastrado."}</span>
                : alunosFiltrados.map((aluno) => (
                  <label key={aluno.id} className="checkbox-item">
                    <input type="checkbox" checked={alunos.includes(aluno.id)} onChange={() => alternar(alunos, setAlunos, aluno.id)} />
                    {aluno.nome}{aluno.rgm && <small> - RGM {aluno.rgm}</small>}{aluno.idTurma && <small> (turma atual: {nomeTurma(aluno.idTurma) || "-"})</small>}
                  </label>
                ))}
            </fieldset>
            {alunos.some((id) => usuarios.find((usuario) => usuario.id === id)?.idTurma) && <p className="turmas-vazio turma-aviso">Alunos que já têm turma serão transferidos para a nova turma.</p>}

            <div className="user-form-actions">
              <button className="button button-primary" type="submit" disabled={saving}>{saving ? "Salvando..." : "Cadastrar turma"}</button>
            </div>
          </form>
        </section>

        <section className="content-card">
          <div className="toolbar">
            <div><span className="eyebrow">Gerenciamento</span><h2>Turmas cadastradas</h2></div>
            <span className="result-count">{turmas.length} {turmas.length === 1 ? "turma" : "turmas"}</span>
          </div>
          {loading ? <Loading text="Carregando turmas..." /> : turmas.length === 0 ? (
            <div className="empty-state"><h2>Nenhuma turma cadastrada.</h2><p>Use o formulário acima para criar a primeira turma.</p></div>
          ) : (
            <div className="table-container">
              <table>
                <thead><tr><th>Nome da turma</th><th className="actions-column">Ações</th></tr></thead>
                <tbody>{turmas.map((turma) => (
                  <tr key={turma.id}>
                    <td><strong>{turma.nome}</strong></td>
                    <td><div className="row-actions"><button className="button button-small button-danger" onClick={() => excluir(turma)}>Excluir</button></div></td>
                  </tr>
                ))}</tbody>
              </table>
            </div>
          )}
        </section>
      </section>
    </main>
  );
}
