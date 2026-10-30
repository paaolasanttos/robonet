import { useEffect, useMemo, useState } from "react";
import Alert from "./aulaModalAlert";
import Loading from "./aulaModalCarregando";
import ConfirmModal from "./aulaModalConfirmar";
import TorneioFormModal from "./torneioModalIncluir";
import TorneioChave from "./torneioChave";
import { periodoTorneio, statusTorneio } from "./torneioFormato";
import { equipesApi, torneiosApi } from "../servicos/api";

function ConfirmarAcao({ titulo, mensagem, textoBotao, loading, onCancel, onConfirm }) {
  return (
    <div className="modal-backdrop" role="presentation">
      <div className="modal modal-small" role="dialog" aria-modal="true" aria-labelledby="confirmar-acao-titulo">
        <div className="modal-header">
          <div>
            <span className="eyebrow">Confirmação</span>
            <h2 id="confirmar-acao-titulo">{titulo}</h2>
          </div>
        </div>

        <p className="confirm-message">{mensagem}</p>

        <div className="modal-footer">
          <button className="button button-secondary" onClick={onCancel} disabled={loading}>Cancelar</button>
          <button className="button button-primary" onClick={onConfirm} disabled={loading}>{loading ? "Salvando..." : textoBotao}</button>
        </div>
      </div>
    </div>
  );
}

export default function TorneioDetalhe({ id, perfil, onVoltar }) {
  const [detalhe, setDetalhe] = useState(null);
  const [equipesProfessor, setEquipesProfessor] = useState([]);
  const [selecionadas, setSelecionadas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");
  const [editOpen, setEditOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [gerarOpen, setGerarOpen] = useState(false);
  const [vencedor, setVencedor] = useState(null);

  function atualizarDetalhe(data) {
    setDetalhe(data);
    setSelecionadas(data.equipes.map((equipe) => equipe.id));
  }

  async function loadDetalhe() {
    setLoading(true);
    try {
      const data = await torneiosApi.buscar(id);
      atualizarDetalhe(data);
      if (data.torneio.podeGerenciar) {
        const equipes = await equipesApi.listar();
        setEquipesProfessor(Array.isArray(equipes) ? equipes : []);
      }
    } catch (err) {
      setError(err.message || "Não foi possível carregar o torneio.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadDetalhe(); }, [id]);

  useEffect(() => {
    if (!feedback) return undefined;
    const timer = setTimeout(() => setFeedback(""), 4000);
    return () => clearTimeout(timer);
  }, [feedback]);

  const equipesPorTurma = useMemo(() => {
    const grupos = {};
    equipesProfessor.forEach((equipe) => {
      const turma = equipe.turma || "Sem turma";
      grupos[turma] = [...(grupos[turma] || []), equipe];
    });
    return grupos;
  }, [equipesProfessor]);

  function toggleEquipe(idEquipe) {
    setSelecionadas((current) => current.includes(idEquipe) ? current.filter((item) => item !== idEquipe) : [...current, idEquipe]);
  }

  async function executar(acao, mensagem) {
    setSaving(true);
    setError("");
    try {
      await acao();
      setFeedback(mensagem);
      return true;
    } catch (err) {
      setError(err.message || "Não foi possível concluir a operação.");
      return false;
    } finally {
      setSaving(false);
    }
  }

  function handleSalvarEquipes() {
    return executar(async () => {
      atualizarDetalhe(await torneiosApi.definirEquipes(id, selecionadas));
    }, "Equipes do torneio salvas.");
  }

  async function handleGerar() {
    await executar(async () => {
      await torneiosApi.definirEquipes(id, selecionadas);
      atualizarDetalhe(await torneiosApi.gerarChaveamento(id));
    }, "Chaveamento gerado com sucesso.");
    setGerarOpen(false);
  }

  async function handleEditar(dados) {
    const ok = await executar(async () => {
      const torneio = await torneiosApi.atualizar(id, dados);
      setDetalhe((current) => ({ ...current, torneio }));
    }, "Torneio atualizado com sucesso.");
    if (ok) setEditOpen(false);
  }

  async function handleVencedor() {
    await executar(async () => {
      atualizarDetalhe(await torneiosApi.registrarVencedor(vencedor.luta.id, vencedor.equipe.id));
    }, `Vitória de ${vencedor.equipe.nome} registrada.`);
    setVencedor(null);
  }

  async function handleExcluir() {
    const ok = await executar(() => torneiosApi.excluir(id), "Torneio excluído.");
    setDeleteOpen(false);
    if (ok) onVoltar();
  }

  if (loading) return <Loading text="Carregando torneio..." />;

  if (!detalhe) {
    return (
      <>
        {error && <Alert type="error" message={error} onClose={() => setError("")} />}
        <button className="button button-secondary" onClick={onVoltar}>Voltar para os torneios</button>
      </>
    );
  }

  const { torneio, equipes, lutas, ranking, totalFases, podeLancarResultado } = detalhe;
  const status = statusTorneio[torneio.status];
  const aberto = torneio.status === "Aberto";
  const selecaoAlterada = selecionadas.length !== equipes.length || selecionadas.some((idEquipe) => !equipes.some((equipe) => equipe.id === idEquipe));

  return (
    <>
      {feedback && <Alert type="success" message={feedback} onClose={() => setFeedback("")} />}
      {error && <Alert type="error" message={error} onClose={() => setError("")} />}

      <section className="content-card torneio-secao">
        <div className="torneio-cabecalho">
          <div>
            <span className="eyebrow">Torneio mata-mata</span>
            <h2>{torneio.nome}</h2>
          </div>
          <span className={`status-pill ${status?.className || ""}`}>{status?.label || torneio.status}</span>
        </div>

        <div className="torneio-info">
          <div><span>Data</span><strong>{periodoTorneio(torneio)}</strong></div>
          <div><span>Local</span><strong>{torneio.local || "-"}</strong></div>
          <div><span>Responsável</span><strong>{torneio.professor}</strong></div>
          <div><span>Equipes</span><strong>{torneio.totalEquipes}</strong></div>
        </div>

        {torneio.regras && <p className="torneio-regras"><strong>Regras:</strong> {torneio.regras}</p>}

        <div className="torneio-acoes">
          <button className="button button-secondary" onClick={onVoltar}>Voltar</button>
          {torneio.podeGerenciar && <button className="button button-light" onClick={() => setEditOpen(true)}>Editar dados</button>}
          {perfil === "admin" && <button className="button button-danger" onClick={() => setDeleteOpen(true)}>Excluir torneio</button>}
        </div>
      </section>

      {aberto && torneio.podeGerenciar && (
        <section className="content-card torneio-secao">
          <div className="section-heading"><span className="eyebrow">Participantes</span><h2>Equipes do torneio</h2></div>
          <p className="torneio-ajuda">Selecione as equipes que vão participar. Podem ser de uma turma só ou de várias turmas suas.</p>

          {equipesProfessor.length === 0 ? (
            <div className="empty-state"><h2>Nenhuma equipe cadastrada.</h2><p>Cadastre as equipes na aba Equipes.</p></div>
          ) : (
            <div className="equipes-selecao">
              {Object.entries(equipesPorTurma).map(([turma, lista]) => (
                <fieldset key={turma} className="equipes-turma" disabled={saving}>
                  <legend>{turma}</legend>
                  {lista.map((equipe) => (
                    <label key={equipe.id} className="checkbox-item">
                      <input type="checkbox" checked={selecionadas.includes(equipe.id)} onChange={() => toggleEquipe(equipe.id)} />
                      {equipe.nome} <small>({equipe.membros.length} {equipe.membros.length === 1 ? "integrante" : "integrantes"})</small>
                    </label>
                  ))}
                </fieldset>
              ))}
            </div>
          )}

          <div className="torneio-acoes">
            <span className="result-count">{selecionadas.length} {selecionadas.length === 1 ? "equipe selecionada" : "equipes selecionadas"}</span>
            <button className="button button-light" onClick={handleSalvarEquipes} disabled={saving || !selecaoAlterada}>Salvar seleção</button>
            <button className="button button-primary" onClick={() => setGerarOpen(true)} disabled={saving || selecionadas.length < 2}>Gerar chaveamento</button>
          </div>
        </section>
      )}

      {aberto && !torneio.podeGerenciar && (
        <section className="content-card torneio-secao">
          <div className="section-heading"><span className="eyebrow">Participantes</span><h2>Equipes inscritas</h2></div>
          {equipes.length === 0 ? <p className="torneio-ajuda">Nenhuma equipe inscrita até o momento.</p> : (
            <ul className="torneio-inscritas">
              {equipes.map((equipe) => <li key={equipe.id}><strong>{equipe.nome}</strong><small>{equipe.turma}</small></li>)}
            </ul>
          )}
          <p className="torneio-ajuda">O chaveamento aparece aqui quando o professor responsável gerar as lutas.</p>
        </section>
      )}

      {!aberto && (
        <section className="content-card torneio-secao">
          <div className="section-heading"><span className="eyebrow">Mata-mata</span><h2>Chaveamento</h2></div>
          {podeLancarResultado && <p className="torneio-ajuda">Clique em "Vencedor" na equipe que ganhou a luta. Ela avança automaticamente para a próxima fase.</p>}
          <TorneioChave lutas={lutas} totalFases={totalFases} podeLancarResultado={podeLancarResultado} onEscolherVencedor={(luta, equipe) => setVencedor({ luta, equipe })} />
        </section>
      )}

      {ranking.length > 0 && (
        <section className="content-card torneio-secao">
          <div className="section-heading"><span className="eyebrow">Ranking</span><h2>{torneio.status === "Finalizado" ? "Classificação final" : "Classificação parcial"}</h2></div>
          <div className="table-container">
            <table>
              <thead><tr><th>Posição</th><th>Equipe</th><th>Turma</th></tr></thead>
              <tbody>
                {ranking.map((item) => (
                  <tr key={item.equipeId} className={item.posicao === 1 ? "ranking-campeao" : ""}>
                    <td><strong>{item.posicao}º</strong>{item.posicao === 1 && " Campeão"}</td>
                    <td>{item.equipe}</td>
                    <td>{item.turma || "-"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {editOpen && <TorneioFormModal torneio={torneio} loading={saving} onClose={() => !saving && setEditOpen(false)} onSubmit={handleEditar} />}

      {deleteOpen && (
        <ConfirmModal
          title="Excluir torneio"
          message="Deseja realmente excluir este torneio? As lutas e a classificação também serão apagadas."
          loading={saving}
          onCancel={() => !saving && setDeleteOpen(false)}
          onConfirm={handleExcluir}
        />
      )}

      {gerarOpen && (
        <ConfirmarAcao
          titulo="Gerar chaveamento"
          mensagem={`As ${selecionadas.length} equipes serão sorteadas nas lutas. Depois disso a lista de equipes e os dados do torneio não poderão mais ser alterados.`}
          textoBotao="Gerar chaveamento"
          loading={saving}
          onCancel={() => !saving && setGerarOpen(false)}
          onConfirm={handleGerar}
        />
      )}

      {vencedor && (
        <ConfirmarAcao
          titulo="Registrar vencedor"
          mensagem={`Confirmar a vitória de ${vencedor.equipe.nome} na luta ${vencedor.luta.ordem}? O resultado não poderá ser alterado.`}
          textoBotao="Confirmar vencedor"
          loading={saving}
          onCancel={() => !saving && setVencedor(null)}
          onConfirm={handleVencedor}
        />
      )}
    </>
  );
}
