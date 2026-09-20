import { useEffect, useState } from "react";
import {
  listarUsuarios,
  criarUsuario,
  mudarPerfil,
  mudarStatus,
  excluirUsuario,
} from "./api";

function App() {
  const [usuarios, setUsuarios] = useState<any[]>([]);
  const [carregando, setCarregando] = useState(true);

  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [rgm, setRgm] = useState("");
  const [perfil, setPerfil] = useState("aluno");
  const [criando, setCriando] = useState(false);

  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState("");

  useEffect(() => {
    carregarLista();
  }, []);

  function carregarLista() {
    setCarregando(true);
    listarUsuarios()
      .then((dados) => {
        setUsuarios(dados);
        setCarregando(false);
      })
      .catch((e) => {
        setErro(e.message);
        setCarregando(false);
      });
  }

  function limparMensagens() {
    setErro("");
    setSucesso("");
  }

  async function handleCriar(e: any) {
    e.preventDefault();
    limparMensagens();

    if (!nome || !email) {
      setErro("Preencha nome e e-mail.");
      return;
    }
    if (perfil === "aluno" && !rgm) {
      setErro("Informe o RGM do aluno.");
      return;
    }

    setCriando(true);
    try {
      await criarUsuario({ nome, email, rgm, perfil });
      setSucesso("Usuário criado com sucesso!");
      setNome("");
      setEmail("");
      setRgm("");
      setPerfil("aluno");
      carregarLista();
    } catch (e: any) {
      setErro(e.message);
    }
    setCriando(false);
  }

  async function handleMudarPerfil(id: number, novoPerfil: string) {
    limparMensagens();
    try {
      await mudarPerfil(id, novoPerfil);
      setSucesso("Perfil atualizado.");
      carregarLista();
    } catch (e: any) {
      setErro(e.message);
    }
  }

  async function handleMudarStatus(id: number, ativo: boolean) {
    limparMensagens();
    try {
      await mudarStatus(id, ativo);
      setSucesso(ativo ? "Usuário ativado." : "Usuário desativado.");
      carregarLista();
    } catch (e: any) {
      setErro(e.message);
    }
  }

  async function handleExcluir(id: number, nomeUsuario: string) {
    if (!confirm("Tem certeza que quer excluir " + nomeUsuario + "?")) return;
    limparMensagens();
    try {
      await excluirUsuario(id);
      setSucesso("Usuário excluído.");
      carregarLista();
    } catch (e: any) {
      setErro(e.message);
    }
  }

  return (
    <div className="container">
      <h1>Administração de usuários</h1>
      <p className="subtitulo">
        Gerencie todos os usuários da plataforma Robonet: crie novos, mude o
        perfil de acesso, ative/desative ou exclua.
      </p>

      {erro && <div className="mensagem-erro">{erro}</div>}
      {sucesso && <div className="mensagem-sucesso">{sucesso}</div>}

      {/* formulario de criar usuario */}
      <div className="form-novo">
        <h2>Novo usuário</h2>
        <form onSubmit={handleCriar}>
          <div className="form-linha">
            <div className="form-campo">
              <label>Nome</label>
              <input
                type="text"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                placeholder="Ex.: João Silva"
              />
            </div>
            <div className="form-campo">
              <label>E-mail</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="joao@umc.com"
              />
            </div>
            <div className="form-campo">
              <label>Perfil</label>
              <select
                value={perfil}
                onChange={(e) => setPerfil(e.target.value)}
              >
                <option value="aluno">Aluno</option>
                <option value="professor">Professor</option>
                <option value="admin">Administrador</option>
              </select>
            </div>
            {/* rgm só aparece se for aluno */}
            {perfil === "aluno" && (
              <div className="form-campo">
                <label>RGM</label>
                <input
                  type="text"
                  value={rgm}
                  onChange={(e) => setRgm(e.target.value)}
                  placeholder="Ex.: 20260102"
                />
              </div>
            )}
          </div>
          <button type="submit" className="btn btn-primario" disabled={criando}>
            {criando ? "Criando..." : "Criar usuário"}
          </button>
        </form>
      </div>

      {/* tabela com a lista */}
      <h2 style={{ fontSize: 18, marginBottom: 12 }}>
        Usuários cadastrados ({usuarios.length})
      </h2>

      {carregando ? (
        <div className="carregando">Carregando...</div>
      ) : usuarios.length === 0 ? (
        <div className="carregando">Nenhum usuário cadastrado ainda.</div>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>E-mail</th>
              <th>RGM</th>
              <th>Perfil</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {usuarios.map((u) => (
              <tr key={u.id}>
                <td>{u.nome}</td>
                <td>{u.email}</td>
                <td>{u.rgm || "-"}</td>
                <td>
                  <select
                    value={u.perfil}
                    onChange={(e) => handleMudarPerfil(u.id, e.target.value)}
                  >
                    <option value="aluno">Aluno</option>
                    <option value="professor">Professor</option>
                    <option value="admin">Administrador</option>
                  </select>
                </td>
                <td>
                  <span className={u.ativo ? "status-ativo" : "status-inativo"}>
                    {u.ativo ? "Ativo" : "Inativo"}
                  </span>
                </td>
                <td>
                  <div className="acoes">
                    <button
                      className="btn btn-secundario"
                      onClick={() => handleMudarStatus(u.id, !u.ativo)}
                    >
                      {u.ativo ? "Desativar" : "Ativar"}
                    </button>
                    <button
                      className="btn btn-perigo"
                      onClick={() => handleExcluir(u.id, u.nome)}
                    >
                      Excluir
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default App;
