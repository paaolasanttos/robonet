import { useEffect, useRef, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { termoApi } from "../servicos/api";
import TermoConteudo, { TERMO_DATA, TERMO_VERSAO } from "../componentes/termoConteudo";

export default function TermoPagina() {
  const { session, login, logout } = useAuth();
  const navigate = useNavigate();
  const caixaRef = useRef(null);
  const [lido, setLido] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function verificarLeitura() {
    const caixa = caixaRef.current;
    if (!caixa) return;
    if (caixa.scrollTop + caixa.clientHeight >= caixa.scrollHeight - 20) setLido(true);
  }

  useEffect(() => { verificarLeitura(); }, []);

  if (!session?.token) return <Navigate to="/login" replace />;
  if (session.usuario?.perfil === "admin") return <Navigate to="/home" replace />;
  if (session.usuario?.termoAceito) return <Navigate to="/home" replace />;

  async function handleAceitar() {
    setError("");
    setLoading(true);
    try {
      const data = await termoApi.aceitar();
      login(data);
      navigate("/home", { replace: true });
    } catch (err) {
      setError(err.message || "Não foi possível registrar o aceite.");
    } finally {
      setLoading(false);
    }
  }

  async function handleRecusar() {
    setLoading(true);
    await termoApi.recusar().catch(() => {});
    logout();
  }

  return (
    <main className="policy-shell">
      <article className="policy-panel">
        <div className="login-brand">
          <span className="brand-mark">R</span>
          <div>
            <strong>ROBONET</strong>
          </div>
        </div>
        <span className="eyebrow">Primeiro acesso</span>
        <h1>Termo de Uso e Aviso de Privacidade</h1>
        <p className="policy-updated">Versão {TERMO_VERSAO} • {TERMO_DATA}</p>
        <p className="termo-instrucao">Olá, {session.usuario?.nome}. Para continuar usando a plataforma, leia todo o documento abaixo até o final. O botão de aceite será liberado após a leitura.</p>
        <div className="termo-box" ref={caixaRef} onScroll={verificarLeitura} tabIndex="0" aria-label="Texto do Termo de Uso e Aviso de Privacidade"><TermoConteudo /></div>
        <p className={`termo-status ${lido ? "termo-status-ok" : ""}`}>{lido ? "Leitura concluída." : "Role o texto até o final para liberar o aceite."}</p>
        <p className="termo-declaracao">Declaro ter 18 anos completos ou mais, li e aceito o Termo de Uso do Robonet e tive acesso ao Aviso de Privacidade.</p>
        {error && <div className="login-error" role="alert">{error}</div>}
        <div className="termo-acoes">
          <button className="button button-secondary" type="button" onClick={handleRecusar} disabled={loading}>Não aceito</button>
          <button className="button button-primary" type="button" onClick={handleAceitar} disabled={!lido || loading}>{loading ? "Registrando..." : "Li e concordo"}</button>
        </div>
        <p className="termo-aviso">Se você não aceitar o termo, não terá acesso a plataforma.</p>
      </article>
    </main>
  );
}