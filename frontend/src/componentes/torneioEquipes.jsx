import { useEffect, useMemo, useState } from "react";
import Alert from "./aulaModalAlert";
import Loading from "./aulaModalCarregando";
import { equipesApi, turmasApi } from "../servicos/api";

const MAXIMO_MEMBROS = 4;
const formularioInicial = { nome: "", turmaId: "", membros: [] };

export default function TorneioEquipes({ perfil }) {
  const podeEditar = perfil === "professor" || perfil === "admin";
  const [equipes, setEquipes] = useState([]);
  const [turmas, setTurmas] = useState([]);
  const [alunos, setAlunos] = useState([]);
  const [form, setForm] = useState(formularioInicial);
  const [editingId, setEditingId] = useState(null);
  const [search, setSearch] = useState("");
  const [buscaAluno, setBuscaAluno] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");

  async function loadEquipes() {
    setLoading(true);
    try {
      const data = await equipesApi.listar();
      setEquipes(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar as equipes.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadEquipes();
    if (podeEditar) {
      (perfil === "admin" ? turmasApi.listar() : turmasApi.minhas())
        .then((data) => setTurmas(Array.isArray(data) ? data : []))
        .catch(() => setTurmas([]));
    }
  }, [podeEditar, perfil]);

  useEffect(() => {
    if (!feedback) return undefined;
    const timer = setTimeout(() => setFeedback(""), 4000);
    return () => clearTimeout(timer);
  }, [feedback]);

  const filteredEquipes = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return equipes;
    return equipes.filter((equipe) =>
      [equipe.nome, equipe.turma, ...equipe.membros.map((membro) => membro.nome)]
        .filter(Boolean).join(" ").toLowerCase().includes(term)
    );
  }, [equipes, search]);

  const alunosFiltrados = useMemo(() => {
    const term = buscaAluno.trim().toLowerCase();
    if (!term) return alunos;
    return alunos.filter((aluno) =>
      form.membros.includes(aluno.id)
      || aluno.nome.toLowerCase().includes(term)
      || String(aluno.rgm || "").includes(term)
    );
  }, [alunos, buscaAluno, form.membros]);

  async function carregarAlunos(turmaId) {
    setAlunos([]);
    setBuscaAluno("");
    if (!turmaId) return;
    try {
      const data = await equipesApi.listarAlunos(turmaId);
      setAlunos(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar os alunos da turma.");
    }
  }

  function updateNome(event) {
    setForm((current) => ({ ...current, nome: event.target.value }));
  }

  function updateTurma(event) {
    const turmaId = event.target.value;
    setForm((current) => ({ ...current, turmaId, membros: [] }));
    carregarAlunos(turmaId);
  }

  function toggleMembro(idAluno) {
    setForm((current) => {
      if (current.membros.includes(idAluno)) {
        return { ...current, membros: current.membros.filter((membro) => membro !== idAluno) };
      }
      if (current.membros.length >= MAXIMO_MEMBROS) return current;
      return { ...current, membros: [...current.membros, idAluno] };
    });
  }

  function nomeEquipe(idEquipe) {
    return equipes.find((equipe) => equipe.id === idEquipe)?.nome || "outra equipe";
  }

  function cancelEditing() {
    setEditingId(null);
    setForm(formularioInicial);
    setAlunos([]);
    setBuscaAluno("");
  }

  function startEditing(equipe) {
    setEditingId(equipe.id);
    setForm({ nome: equipe.nome, turmaId: String(equipe.turmaId), membros: equipe.membros.map((membro) => membro.id) });
    carregarAlunos(equipe.turmaId);
    setError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    if (!form.nome.trim() || !form.turmaId) {
      setError("Informe o nome e a turma da equipe.");
      return;
    }
    if (form.membros.length === 0) {
      setError("Selecione pelo menos 1 integrante.");
      return;
    }

    setSaving(true);
    try {
      const dados = { nome: form.nome.trim(), turmaId: Number(form.turmaId), membros: form.membros };
      if (editingId) {
        const equipe = await equipesApi.atualizar(editingId, dados);
        setEquipes((current) => current.map((item) => item.id === editingId ? equipe : item));
        setFeedback("Equipe atualizada com sucesso.");
      } else {
        const equipe = await equipesApi.criar(dados);
        setEquipes((current) => [...current, equipe].sort((a, b) => a.nome.localeCompare(b.nome)));
        setFeedback("Equipe criada com sucesso.");
      }
      cancelEditing();
    } catch (err) {
      setError(err.message || "Não foi possível salvar a equipe.");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(equipe) {
    if (!window.confirm(`Excluir a equipe ${equipe.nome}?`)) return;
    setError("");
    try {
      await equipesApi.excluir(equipe.id);
      setEquipes((current) => current.filter((item) => item.id !== equipe.id));
      if (editingId === equipe.id) cancelEditing();
      setFeedback("Equipe excluída com sucesso.");
    } catch (err) {
      setError(err.message || "Não foi possível excluir a equipe.");
    }
  }

  return (
    <>
      {feedback && <Alert type="success" message={feedback} onClose={() => setFeedback("")} />}
      {error && <Alert type="error" message={error} onClose={() => setError("")} />}

      {podeEditar && (
        <section className="content-card user-create-card">
          <div className="section-heading"><span className="eyebrow">Equipes</span><h2>{editingId ? "Editar equipe" : "Nova equipe"}</h2></div>

          {turmas.length === 0 ? (
            <p className="torneio-ajuda">{perfil === "admin" ? "Nenhuma turma cadastrada. Cadastre as turmas na tela de Turmas." : "Você ainda não está vinculado a nenhuma turma. Peça ao administrador para vincular suas turmas na tela de Usuários."}</p>
          ) : (
            <form className="equipe-form" onSubmit={handleSubmit}>
              <div className="form-field">
                <label htmlFor="equipe-nome">Nome da equipe *</label>
                <input id="equipe-nome" value={form.nome} onChange={updateNome} placeholder="Ex.: Robôs do Futuro" maxLength="120" disabled={saving} />
              </div>

              <div className="form-field">
                <label htmlFor="equipe-turma">Turma *</label>
                <select id="equipe-turma" value={form.turmaId} onChange={updateTurma} disabled={saving}>
                  <option value="">Selecione a turma</option>
                  {turmas.map((turma) => <option key={turma.id} value={turma.id}>{turma.nome}</option>)}
                </select>
              </div>

              <div className="form-field equipe-busca">
                <label htmlFor="equipe-busca-aluno">Pesquisar aluno</label>
                <input id="equipe-busca-aluno" type="search" value={buscaAluno} onChange={(event) => setBuscaAluno(event.target.value)} placeholder="Nome ou RGM do aluno" disabled={saving || !form.turmaId} />
              </div>

              <fieldset className="equipe-alunos" disabled={saving}>
                <legend>Integrantes ({form.membros.length}/{MAXIMO_MEMBROS})</legend>
                {!form.turmaId ? <span className="turmas-vazio">Selecione a turma para ver os alunos.</span>
                  : alunos.length === 0 ? <span className="turmas-vazio">Nenhum aluno ativo nesta turma.</span>
                  : alunosFiltrados.length === 0 ? <span className="turmas-vazio">Nenhum aluno encontrado com esse nome ou RGM.</span>
                  : alunosFiltrados.map((aluno) => {
                    const emOutraEquipe = aluno.equipeId && aluno.equipeId !== editingId;
                    const selecionado = form.membros.includes(aluno.id);
                    return (
                      <label key={aluno.id} className="checkbox-item">
                        <input type="checkbox" checked={selecionado} disabled={emOutraEquipe || (!selecionado && form.membros.length >= MAXIMO_MEMBROS)} onChange={() => toggleMembro(aluno.id)} />
                        {aluno.nome}{aluno.rgm && <small> - RGM {aluno.rgm}</small>}{emOutraEquipe && <small> (já está na equipe {nomeEquipe(aluno.equipeId)})</small>}
                      </label>
                    );
                  })}
              </fieldset>

              <div className="user-form-actions">
                <button className="button button-primary" type="submit" disabled={saving}>{saving ? "Salvando..." : editingId ? "Salvar alterações" : "Criar equipe"}</button>
                {editingId && <button className="button button-light" type="button" onClick={cancelEditing} disabled={saving}>Cancelar edição</button>}
              </div>
            </form>
          )}
        </section>
      )}

      <section className="content-card">
        <div className="toolbar">
          <div className="search-wrapper">
            <label htmlFor="search-equipes">Pesquisar equipes</label>
            <input id="search-equipes" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Equipe, turma ou integrante..." />
          </div>
          <span className="result-count">{filteredEquipes.length} {filteredEquipes.length === 1 ? "equipe" : "equipes"}</span>
        </div>

        {loading ? <Loading text="Carregando equipes..." /> : filteredEquipes.length === 0 ? (
          <div className="empty-state"><h2>Nenhuma equipe encontrada.</h2><p>{search ? "Tente alterar os termos da pesquisa." : podeEditar ? "Cadastre a primeira equipe acima." : "Ainda não há equipes cadastradas."}</p></div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr><th>Equipe</th><th>Turma</th><th>Integrantes</th><th>Situação</th>{podeEditar && <th className="actions-column">Ações</th>}</tr>
              </thead>
              <tbody>
                {filteredEquipes.map((equipe) => (
                  <tr key={equipe.id}>
                    <td><strong>{equipe.nome}</strong></td>
                    <td>{equipe.turma || "-"}</td>
                    <td className="description-cell">{equipe.membros.map((membro) => membro.nome).join(", ")}</td>
                    <td><span className={`status-pill ${equipe.emTorneio ? "status-andamento" : "status-active"}`}>{equipe.emTorneio ? "Inscrita em torneio" : "Disponível"}</span></td>
                    {podeEditar && (
                      <td>
                        <div className="row-actions">
                          <button className="button button-small button-light" onClick={() => startEditing(equipe)}>Editar</button>
                          {!equipe.emTorneio && <button className="button button-small button-danger" onClick={() => handleDelete(equipe)}>Excluir</button>}
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}
