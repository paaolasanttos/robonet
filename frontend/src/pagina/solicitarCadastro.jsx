import { useState } from "react";
import { Link } from "react-router-dom";
import { authApi } from "../servicos/api";
import SenhaRequisitos from "../componentes/senhaRequisitos";
import { MENSAGEM_SENHA, senhaValida } from "../componentes/senhaRegras";

const formularioInicial = { nome: "", email: "", perfil: "aluno", ra: "", senha: "", confirmacao: "", maiorIdade: false, termoLido: false };

export default function SolicitarCadastroPagina() {
  const [form, setForm] = useState(formularioInicial);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [enviado, setEnviado] = useState(false);

  function atualizar(event) {
    const { name, value, type, checked } = event.target;
    setForm((atual) => ({
      ...atual,
      [name]: type === "checkbox" ? checked : name === "ra" ? value.replace(/\D/g, "").slice(0, 11) : value,
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setMessage("");
    setError("");
    if (!form.nome.trim() || !form.email.trim()) {
      setError("Preencha nome e e-mail.");
      return;
    }
    if (form.perfil === "aluno" && form.ra.length !== 11) {
      setError("O RA deve ter exatamente 11 números.");
      return;
    }
    if (!senhaValida(form.senha)) {
      setError(MENSAGEM_SENHA);
      return;
    }
    if (form.senha !== form.confirmacao) {
      setError("As senhas não coincidem.");
      return;
    }

    setLoading(true);
    try {
      const data = await authApi.solicitarCadastro({
        nome: form.nome.trim(),
        email: form.email.trim(),
        perfil: form.perfil,
        ra: form.perfil === "aluno" ? form.ra : null,
        senha: form.senha,
      });
      setMessage(data?.message || "Solicitação enviada. Aguarde a aprovação do administrador.");
      setForm(formularioInicial);
      setEnviado(true);
    } catch (err) {
      setError(err.message || "Não foi possível enviar a solicitação.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-shell">
      <section className="login-panel">
        <div className="login-brand">
          <span className="brand-mark">R</span>
          <div>
            <strong>ROBONET</strong>
          </div>
        </div>
        <div className="login-heading">
          <h1>Solicitar cadastro</h1>
          <p>Preencha seus dados. O administrador vai analisar o pedido e, se aprovado, você entra com o e-mail e a senha escolhidos aqui.</p>
        </div>
        {message && <div className="login-success" role="status">{message}</div>}
        {error && <div className="login-error" role="alert">{error}</div>}
        {!enviado && (
          <form onSubmit={handleSubmit} className="login-form">
            <div className="form-field">
              <label htmlFor="cadastro-nome">Nome completo</label>
              <input id="cadastro-nome" name="nome" value={form.nome} onChange={atualizar} maxLength="50" autoComplete="name" required disabled={loading} />
            </div>
            <div className="form-field">
              <label htmlFor="cadastro-email">E-mail</label>
              <input id="cadastro-email" name="email" type="email" value={form.email} onChange={atualizar} maxLength="255" autoComplete="email" required disabled={loading} />
            </div>
            <div className="form-field">
              <label htmlFor="cadastro-perfil">Perfil</label>
              <select id="cadastro-perfil" name="perfil" value={form.perfil} onChange={atualizar} disabled={loading}>
                <option value="aluno">Aluno</option>
                <option value="professor">Professor</option>
              </select>
            </div>
            {form.perfil === "aluno" && (
              <div className="form-field">
                <label htmlFor="cadastro-ra">RA (Registro do Aluno)</label>
                <input id="cadastro-ra" name="ra" value={form.ra} onChange={atualizar} inputMode="numeric" maxLength="11" placeholder="11 números" required disabled={loading} />
              </div>
            )}
            <div className="form-field">
              <label htmlFor="cadastro-senha">Senha</label>
              <input id="cadastro-senha" name="senha" type="password" value={form.senha} onChange={atualizar} autoComplete="new-password" required disabled={loading} />
              <SenhaRequisitos senha={form.senha} />
            </div>
            <div className="form-field">
              <label htmlFor="cadastro-confirmacao">Confirme a senha</label>
              <input id="cadastro-confirmacao" name="confirmacao" type="password" value={form.confirmacao} onChange={atualizar} autoComplete="new-password" required disabled={loading} />
            </div>
            <label className="checkbox-item">
              <input type="checkbox" name="maiorIdade" checked={form.maiorIdade} onChange={atualizar} required disabled={loading} />
              Declaro ter 18 anos completos ou mais.
            </label>
            <label className="checkbox-item">
              <input type="checkbox" name="termoLido" checked={form.termoLido} onChange={atualizar} required disabled={loading} />
              <span>Li o <Link to="/politicas" target="_blank" rel="noopener noreferrer">Termo de Uso e Aviso de Privacidade</Link>. Meus dados serão usados apenas para analisar o pedido e criar a conta, e a senha é guardada somente em formato de hash.</span>
            </label>
            <button className="button button-primary recovery-button" type="submit" disabled={loading || !form.maiorIdade || !form.termoLido}>
              {loading ? "Enviando..." : "Enviar solicitação"}
            </button>
          </form>
        )}

        <div className="recovery-actions">
          <Link className="button button-secondary recovery-button" to="/login">Voltar para o login</Link>
        </div>
      </section>
    </main>
  );
}
