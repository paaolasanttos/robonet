import { requisitosSenha } from "./senhaRegras";

export default function SenhaRequisitos({ senha }) {
  return (
    <ul className="senha-requisitos" aria-label="Requisitos da senha">
      {requisitosSenha.map((requisito) => {
        const atendido = requisito.atendido(senha);
        return (
          <li key={requisito.chave} className={atendido ? "senha-requisito-ok" : ""}>
            <span className="senha-requisito-marca" aria-hidden="true">{atendido ? "✓" : ""}</span>
            {requisito.texto}
            <span className="sr-only">{atendido ? " (atendido)" : " (pendente)"}</span>
          </li>
        );
      })}
    </ul>
  );
}
