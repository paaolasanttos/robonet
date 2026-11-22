import { useCallback, useEffect, useMemo, useState } from "react";
import Alert from "../componentes/aulaModalAlert";
import Loading from "../componentes/aulaModalCarregando";
import Paginacao, { REGISTROS_POR_PAGINA } from "../componentes/paginacao";
import { turmasApi, usuariosApi } from "../servicos/api";
import MenuPadrao from "../componentes/menuPadrao";
import RodapePadrao from "../componentes/rodapePadrao";

export default function TurmasPagina() {
  const [turmas, setTurmas] = useState([]);
  const [nome, setNome] = useState("");
  const [usuarios, setUsuarios] = useState([]);
  const [professores, setProfessores] = useState([]);
  const [alunos, setAlunos] = useState([]);
  const [buscaAluno, setBuscaAluno] = useState("");
  const [buscaTurma, setBuscaTurma] = useState("");
  const [pagina, setPagina] = useState(1);
  const [formOpen, setFormOpen] = useState(false);
  const [editandoId, setEditandoId] = useState(null);
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

  const alunosOrdenados = useMemo(() => {
    const grupo = (aluno) => (!aluno.idTurma ? 0 : aluno.idTurma === editandoId ? 1 : 2);
    return usuarios
      .filter((usuario) => usuario.perfil === "aluno")
      .sort((a, b) => grupo(a) - grupo(b) || a.nome.localeCompare(b.nome));
  }, [usuarios, editandoId]);

  const alunosFiltrados = useMemo(() => {
    const term = buscaAluno.trim().toLowerCase();
    if (!term) return alunosOrdenados;
    return alunosOrdenados.filter((aluno) =>
      alunos.includes(aluno.id)
      || aluno.nome.toLowerCase().includes(term)
      || String(aluno.ra || "").includes(term)
    );
  }, [alunosOrdenados, buscaAluno, alunos]);

  const turmasFiltradas = useMemo(() => {
    const term = buscaTurma.trim().toLowerCase();
    return term ? turmas.filter((turma) => turma.nome.toLowerCase().includes(term)) : turmas;
  }, [turmas, buscaTurma]);
  const totalPaginas = Math.max(1, Math.ceil(turmasFiltradas.length / REGISTROS_POR_PAGINA));
  const paginaAtual = Math.min(pagina, totalPaginas);
  const turmasDaPagina = turmasFiltradas.slice(
    (paginaAtual - 1) * REGISTROS_POR_PAGINA,
    paginaAtual * REGISTROS_POR_PAGINA
  );

  const semTurma = alunosFiltrados.filter((aluno) => !aluno.idTurma);
  const comTurma = alunosFiltrados.filter((aluno) => aluno.idTurma);

  function alternar(lista, setLista, id) {
    setLista(lista.includes(id) ? lista.filter((item) => item !== id) : [...lista, id]);
  }

  function nomeTurma(id) {
    return turmas.find((turma) => turma.id === id)?.nome;
  }

  function professoresDaTurma(id) {
    return professoresAtivos.filter((professor) => professor.turmas?.includes(id));
  }

  function totalAlunos(id) {
    return usuarios.filter((usuario) => usuario.perfil === "aluno" && usuario.idTurma === id).length;
  }

  useEffect(() => {
    if (!feedback) return undefined;
    const timer = setTimeout(() => setFeedback(""), 4000);
    return () => clearTimeout(timer);
  }, [feedback]);

  function limparFormulario() {
    setEditandoId(null);
    setNome("");
    setProfessores([]);
    setAlunos([]);
    setBuscaAluno("");
    setFormOpen(false);
  }

  function abrirCadastro() {
    limparFormulario();
    setError("");
    setFormOpen(true);
  }

  function editar(turma) {
    setError("");
    setEditandoId(turma.id);
    setNome(turma.nome);
    setProfessores(professoresDaTurma(turma.id).map((professor) => professor.id));
    setAlunos(usuarios.filter((usuario) => usuario.perfil === "aluno" && usuario.idTurma === turma.id).map((aluno) => aluno.id));
    setBuscaAluno("");
    setFormOpen(true);
  }

  async function salvar(event) {
    event.preventDefault();
    const nomeInformado = nome.trim();
    if (!nomeInformado) {
      setError("Informe o nome da turma.");
      return;
    }

    setSaving(true);
    setError("");
    try {
      if (editandoId) {
        const turma = await turmasApi.atualizar(editandoId, { nome: nomeInformado, professores, alunos });
        setTurmas((atuais) => atuais.map((item) => item.id === turma.id ? turma : item).sort((a, b) => a.nome.localeCompare(b.nome)));
        setFeedback("Turma atualizada com sucesso.");
      } else {
        const turma = await turmasApi.criar({ nome: nomeInformado, professores, alunos });
        setTurmas((atuais) => [...atuais, turma].sort((a, b) => a.nome.localeCompare(b.nome)));
        setFeedback("Turma cadastrada com sucesso.");
      }
      limparFormulario();
      carregarUsuarios();
    } catch (err) {
      setError(err.message || "Não foi possível salvar a turma.");
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
      if (editandoId === turma.id) limparFormulario();
      carregarUsuarios();
      setFeedback("Turma excluída com sucesso.");
    } catch (err) {
      setError(err.message || "Não foi possível excluir a turma.");
    }
  }

  function itemAluno(aluno) {
    return (
      <label key={aluno.id} className="checkbox-item lista-alunos-item">
        <input type="checkbox" checked={alunos.includes(aluno.id)} onChange={() => alternar(alunos, setAlunos, aluno.id)} />
        <span>{aluno.nome}{aluno.ra && <small> - RA {aluno.ra}</small>}{aluno.idTurma && <small className="lista-alunos-turma"> (turma atual: {nomeTurma(aluno.idTurma) || "-"})</small>}</span>
      </label>
    );
  }

  const transferidos = alunos.some((id) => {
    const turmaAtual = usuarios.find((usuario) => usuario.id === id)?.idTurma;
    return turmaAtual && turmaAtual !== editandoId;
  });

  return (
    <main className="app-shell">
      <MenuPadrao />

      <section className="page">
        <header className="page-header">
          <div><span className="eyebrow">ROBONET</span><h1>Cadastro de Turma</h1><p>Cadastre e gerencie as turmas da plataforma.</p></div>
          <button className="button button-primary" type="button" onClick={abrirCadastro}>+ Nova turma</button>
        </header>
        {feedback && <Alert type="success" message={feedback} onClose={() => setFeedback("")} />}
        {error && <Alert type="error" message={error} onClose={() => setError("")} />}

        <section className="content-card">
          <div className="toolbar">
            <div className="search-wrapper">
              <label htmlFor="busca-turmas">Pesquisar turmas</label>
              <input id="busca-turmas" type="search" value={buscaTurma} onChange={(event) => { setBuscaTurma(event.target.value); setPagina(1); }} placeholder="Digite o nome da turma..." />
            </div>
            <span className="result-count">{turmasFiltradas.length} {turmasFiltradas.length === 1 ? "turma" : "turmas"}</span>
          </div>
          {loading ? <Loading text="Carregando turmas..." /> : turmasFiltradas.length === 0 ? (
            <div className="empty-state"><h2>{buscaTurma ? "Nenhuma turma encontrada." : "Nenhuma turma cadastrada."}</h2>{buscaTurma && <p>Tente alterar os termos da pesquisa.</p>}</div>
          ) : (
            <div className="table-container">
              <table className="tabela-responsiva">
                <thead><tr><th>Nome da turma</th><th>Professores</th><th>Alunos</th><th className="actions-column">Ações</th></tr></thead>
                <tbody>{turmasDaPagina.map((turma) => (
                  <tr key={turma.id}>
                    <td data-label="Turma"><strong>{turma.nome}</strong></td>
                    <td data-label="Professores">{professoresDaTurma(turma.id).map((professor) => professor.nome).join(", ") || "-"}</td>
                    <td data-label="Alunos">{totalAlunos(turma.id)}</td>
                    <td data-label="Ações"><div className="row-actions">
                      <button className="button button-small button-light" onClick={() => editar(turma)} disabled={saving}>Editar</button>
                      <button className="button button-small button-danger" onClick={() => excluir(turma)} disabled={saving}>Excluir</button>
                    </div></td>
                  </tr>
                ))}</tbody>
              </table>
            </div>
          )}
          {!loading && turmasFiltradas.length > 0 && <Paginacao pagina={paginaAtual} total={turmasFiltradas.length} aoMudar={setPagina} />}
        </section>

        {formOpen && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && !saving && limparFormulario()}>
          <section className="modal" role="dialog" aria-modal="true" aria-labelledby="turma-form-titulo">
            <div className="modal-header">
              <div><span className="eyebrow">{editandoId ? "Edição" : "Cadastro"}</span><h2 id="turma-form-titulo">{editandoId ? `Editar turma ${nomeTurma(editandoId) || ""}` : "Cadastro nova turma"}</h2></div>
              <button type="button" className="icon-button" onClick={limparFormulario} disabled={saving} aria-label="Fechar">×</button>
            </div>
          <form className="equipe-form" onSubmit={salvar}>
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
              <input id="turma-busca-aluno" type="search" value={buscaAluno} onChange={(event) => setBuscaAluno(event.target.value)} placeholder="Nome ou RA do aluno" disabled={saving} />
            </div>

            <fieldset className="lista-alunos" disabled={saving}>
              <legend>Alunos selecionados ({alunos.length})</legend>
              <div className="lista-alunos-rolagem" tabIndex="0" aria-label="Lista de alunos">
                {alunosFiltrados.length === 0 ? <span className="turmas-vazio">{buscaAluno.trim() ? "Nenhum aluno encontrado com esse nome ou RA." : "Nenhum aluno ativo cadastrado."}</span> : (
                  <>
                    {semTurma.length > 0 && <p className="lista-alunos-grupo">Sem turma ({semTurma.length})</p>}
                    {semTurma.map(itemAluno)}
                    {comTurma.length > 0 && <p className="lista-alunos-grupo">Já estão em uma turma ({comTurma.length})</p>}
                    {comTurma.map(itemAluno)}
                  </>
                )}
              </div>
            </fieldset>
            {transferidos && <p className="turmas-vazio turma-aviso">Alunos que já têm outra turma serão transferidos para esta turma.</p>}
            {editandoId && <p className="turmas-vazio">Alunos desmarcados sairão da turma. Alunos que fazem parte de uma equipe precisam ser retirados da equipe antes.</p>}

            <div className="user-form-actions">
              <button className="button button-primary" type="submit" disabled={saving}>{saving ? "Salvando..." : editandoId ? "Salvar alterações" : "Cadastrar turma"}</button>
              <button className="button button-light" type="button" onClick={limparFormulario} disabled={saving}>Cancelar</button>
            </div>
          </form>
          </section>
        </div>}
      </section>
      <RodapePadrao />
    </main>
  );
}
