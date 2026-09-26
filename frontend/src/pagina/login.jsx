import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { authApi } from "../servicos/api";

export default function LoginPagina() {
  const { session, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [codigo, setCodigo] = useState("");
  const [codeRequested, setCodeRequested] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  if (session?.token) return <Navigate to="/aulas" replace />;

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      if (!codeRequested) {
        await authApi.solicitarCodigo({ email, senha });
        setCodeRequested(true);
        return;
      }
      const data = await authApi.verificarCodigo({ email, codigo });
      login(data);
      navigate(location.state?.from || "/aulas", { replace: true });
    } catch (err) {
      setError(err.message || "Não foi possível entrar.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-shell">
      <section className="login-panel">
        <div className="login-brand"><span className="brand-mark">R</span><div><strong>ROBONET</strong><small>Gestão acadêmica</small></div></div>
        <div className="login-heading"><span className="eyebrow">Acesso seguro</span><h1>{codeRequested ? "Confirme seu acesso" : "Entrar na plataforma"}</h1><p>{codeRequested ? `Informe o código enviado para ${email}.` : "Use suas credenciais para receber um código de acesso por e-mail."}</p></div>
        {error && <div className="login-error" role="alert">{error}</div>}
        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-field"><label htmlFor="login-email">E-mail</label><input id="login-email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="username" required /></div>
          {!codeRequested && <div className="form-field"><label htmlFor="login-password">Senha</label><input id="login-password" type="password" value={senha} onChange={(event) => setSenha(event.target.value)} autoComplete="current-password" required /></div>}
          {codeRequested && <div className="form-field"><label htmlFor="login-code">Código de acesso</label><input id="login-code" inputMode="numeric" pattern="[0-9]{6}" maxLength="6" value={codigo} onChange={(event) => setCodigo(event.target.value.replace(/\D/g, ""))} autoComplete="one-time-code" placeholder="000000" required /></div>}
          <button className="button button-primary" type="submit" disabled={loading}>{loading ? (codeRequested ? "Validando..." : "Enviando código...") : (codeRequested ? "Validar código" : "Acessar")}</button>
          {codeRequested && <button className="login-back" type="button" onClick={() => { setCodeRequested(false); setCodigo(""); setError(""); }}>Usar outro e-mail</button>}
        </form>
        {!codeRequested && <div className="login-options" aria-label="Opções de acesso"><Link to="/recuperar-senha">Esqueci minha senha / primeiro acesso</Link><Link to="/politicas">Políticas de uso</Link></div>}
      </section>
    </main>
  );
}
