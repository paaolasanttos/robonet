import { Link } from "react-router-dom";

export default function AcessoNegadoPagina() {
  return <main className="login-shell"><section className="login-panel"><span className="eyebrow">Acesso restrito</span><h1>Permissão insuficiente</h1><p>Sua conta não possui autorização para administrar usuários.</p><Link className="button button-primary" to="/aulas">Voltar para aulas</Link></section></main>;
}
