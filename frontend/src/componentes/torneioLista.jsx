import { useEffect, useMemo, useState } from "react";
import Alert from "./aulaModalAlert";
import Loading from "./aulaModalCarregando";
import TorneioFormModal from "./torneioModalIncluir";
import { periodoTorneio, statusTorneio } from "./torneioFormato";
import { torneiosApi } from "../servicos/api";

export default function TorneioLista({ perfil, onAbrir }) {
  const [torneios, setTorneios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [formOpen, setFormOpen] = useState(false);

  async function loadTorneios() {
    setLoading(true);
    try {
      const data = await torneiosApi.listar();
      setTorneios(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Não foi possível carregar os torneios.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadTorneios(); }, []);

  const filteredTorneios = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return torneios;
    return torneios.filter((torneio) =>
      [torneio.nome, torneio.local, torneio.professor]
        .filter(Boolean).join(" ").toLowerCase().includes(term)
    );
  }, [torneios, search]);

  async function handleCreate(dados) {
    setSaving(true);
    setError("");
    try {
      const torneio = await torneiosApi.criar(dados);
      setFormOpen(false);
      onAbrir(torneio.id);
    } catch (err) {
      setError(err.message || "Não foi possível criar o torneio.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      {error && <Alert type="error" message={error} onClose={() => setError("")} />}

      <section className="content-card">
        <div className="toolbar">
          <div className="search-wrapper">
            <label htmlFor="search-torneios">Pesquisar torneios</label>
            <input id="search-torneios" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Nome, local ou professor..." />
          </div>
          <span className="result-count">{filteredTorneios.length} {filteredTorneios.length === 1 ? "torneio" : "torneios"}</span>
          {perfil !== "aluno" && <button className="button button-primary" onClick={() => setFormOpen(true)}>+ Novo torneio</button>}
        </div>

        {loading ? <Loading text="Carregando torneios..." /> : filteredTorneios.length === 0 ? (
          <div className="empty-state">
            <h2>Nenhum torneio encontrado.</h2>
            <p>{search ? "Tente alterar os termos da pesquisa." : perfil !== "aluno" ? "Crie o primeiro torneio no botão acima." : "Ainda não há torneios cadastrados."}</p>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr><th>Torneio</th><th>Data</th><th>Local</th><th>Equipes</th><th>Status</th><th className="actions-column">Ações</th></tr>
              </thead>
              <tbody>
                {filteredTorneios.map((torneio) => (
                  <tr key={torneio.id}>
                    <td><div className="title-cell"><strong>{torneio.nome}</strong><span>Responsável: {torneio.professor}</span></div></td>
                    <td className="auditoria-data">{periodoTorneio(torneio)}</td>
                    <td>{torneio.local || "-"}</td>
                    <td>{torneio.totalEquipes}</td>
                    <td><span className={`status-pill ${statusTorneio[torneio.status]?.className || ""}`}>{statusTorneio[torneio.status]?.label || torneio.status}</span></td>
                    <td><div className="row-actions"><button className="button button-small button-light" onClick={() => onAbrir(torneio.id)}>{torneio.podeGerenciar ? "Gerenciar" : perfil === "professor" ? "Abrir" : "Visualizar"}</button></div></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {formOpen && <TorneioFormModal loading={saving} onClose={() => !saving && setFormOpen(false)} onSubmit={handleCreate} />}
    </>
  );
}
