import { Link } from "react-router-dom";

export default function PoliticasPagina() {
  return (
    <main className="policy-shell">
      <article className="policy-panel">
        <div className="login-brand"><span className="brand-mark">R</span><div><strong>ROBONET</strong><small>Gestão acadêmica</small></div></div>
        <span className="eyebrow">Transparência</span>
        <h1>Políticas de uso</h1>
        <p className="policy-updated">Última atualização: 22 de setembro de 2026</p>
        <section><h2>Uso da plataforma</h2><p>A ROBONET deve ser utilizada para atividades acadêmicas e administrativas autorizadas. Cada pessoa é responsável pelas informações e ações realizadas com sua conta.</p></section>
        <section><h2>Conta e segurança</h2><p>Não compartilhe sua senha ou código de acesso. O acesso é protegido por autenticação em duas etapas, e a equipe administrativa pode suspender contas que violem estas regras.</p></section>
        <section><h2>Dados pessoais</h2><p>Os dados cadastrados são usados para identificação, controle de acesso e operação dos recursos acadêmicos. O tratamento deve respeitar as finalidades institucionais e a legislação aplicável.</p></section>
        <section><h2>Responsabilidades</h2><p>Não é permitido tentar acessar contas, dados ou funcionalidades sem autorização, nem inserir conteúdo que comprometa a disponibilidade ou segurança da plataforma.</p></section>
        <Link className="button button-primary" to="/login">Voltar para o login</Link>
      </article>
    </main>
  );
}
