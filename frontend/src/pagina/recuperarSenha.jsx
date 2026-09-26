import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authApi } from "../servicos/api";

export default function RecuperarSenhaPagina() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [codigo, setCodigo] = useState("");
  const [senha, setSenha] = useState("");
  const [confirmacao, setConfirmacao] = useState("");
  const [codeRequested, setCodeRequested] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setMessage("");
    setError("");
    setLoading(true);
    try {
      if (!codeRequested) {
        await authApi.solicitarCodigoSenha({ email });
        setCodeRequested(true);
        setMessage("Confira seu e-mail e informe o código recebido.");
      } else {
        if (senha !== confirmacao) throw new Error("As senhas não coincidem.");
        await authApi.redefinirSenha({ email, codigo, senha });
        setMessage("Senha definida com sucesso. Redirecionando para o login...");
        setTimeout(() => navigate("/login"), 1200);
      }
    } catch (err) {
      setError(err.message || "Não foi possível concluir a operação.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-shell">
      <section className="login-panel">
        <div className="login-brand"><span className="brand-mark">R</span><div><strong>ROBONET</strong><small>Gestão acadêmica</small></div></div>
        <div className="login-heading"><span className="eyebrow">Primeiro acesso</span><h1>{codeRequested ? "Defina sua senha" : "Recupere seu acesso"}</h1><p>{codeRequested ? "Use o código enviado para criar uma nova senha." : "Enviaremos um código para o e-mail cadastrado."}</p></div>
        {message && <div className="login-success" role="status">{message}</div>}
        {error && <div className="login-error" role="alert">{error}</div>}
        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-field"><label htmlFor="recovery-email">E-mail</label><input id="recovery-email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="email" required disabled={codeRequested} /></div>
          {codeRequested && <><div className="form-field"><label htmlFor="recovery-code">Código de acesso</label><input id="recovery-code" inputMode="numeric" pattern="[0-9]{6}" maxLength="6" value={codigo} onChange={(event) => setCodigo(event.target.value.replace(/\D/g, ""))} autoComplete="one-time-code" required /></div><div className="form-field"><label htmlFor="new-password">Nova senha</label><input id="new-password" type="password" minLength="8" value={senha} onChange={(event) => setSenha(event.target.value)} autoComplete="new-password" required /></div><div className="form-field"><label htmlFor="confirm-password">Confirme a nova senha</label><input id="confirm-password" type="password" minLength="8" value={confirmacao} onChange={(event) => setConfirmacao(event.target.value)} autoComplete="new-password" required /></div></>}
          <button className="button button-primary" type="submit" disabled={loading}>{loading ? "Aguarde..." : codeRequested ? "Definir senha" : "Enviar código"}</button>
          {codeRequested && <button className="login-back" type="button" onClick={() => { setCodeRequested(false); setCodigo(""); setMessage(""); }}>Usar outro e-mail</button>}
        </form>
        <Link className="login-link" to="/login">Voltar para o login</Link>
      </section>
    </main>
  );
}
