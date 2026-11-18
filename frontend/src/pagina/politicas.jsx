import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import TermoConteudo, { TERMO_DATA, TERMO_VERSAO } from "../componentes/termoConteudo";

export default function PoliticasPagina() {
  const { session } = useAuth();

  return (
    <main className="policy-shell">
      <article className="policy-panel">
        <div className="login-brand">
          <span className="brand-mark">R</span>
          <div>
            <strong>ROBONET</strong>
          </div>
        </div>
        <span className="eyebrow">Transparência e LGPD</span>
        <h1>Termo de Uso e Aviso de Privacidade</h1>
        <p className="policy-updated">Versão {TERMO_VERSAO} • {TERMO_DATA}</p>
        <nav className="termo-indice" aria-label="Seções do documento">
          <a href="#home">Inicio</a>
          <a href="#regras-de-uso">Regras de uso</a>
          <a href="#privacidade">Aviso de Privacidade</a>
          <a href="#google">Google e compartilhamento</a>
          <a href="#direitos">Seus direitos</a>
        </nav>
        <TermoConteudo />
        <Link className="button button-primary" to={session?.token ? "/home" : "/login"}>{session?.token ? "Voltar para a plataforma" : "Voltar para o login"}</Link>
      </article>
    </main>
  );
}