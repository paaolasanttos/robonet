import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import TermoConteudo, { formatarDataTermo, titulosTermo, useTermoAtual } from "../componentes/termoConteudo";

export default function PoliticasPagina() {
  const { session } = useAuth();
  const { termo, erro } = useTermoAtual();

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
        {termo && <p className="policy-updated">Versão {termo.versao} • {formatarDataTermo(termo.dtVersao)}</p>}
        <nav className="termo-indice" aria-label="Seções do documento">
          {titulosTermo(termo?.conteudo).map((item) => <a key={item.id} href={`#${item.id}`}>{item.titulo}</a>)}
        </nav>
        <TermoConteudo termo={termo} erro={erro} />
        <Link className="button button-primary" to={session?.token ? "/home" : "/login"}>{session?.token ? "Voltar para a plataforma" : "Voltar para o login"}</Link>
      </article>
    </main>
  );
}