import { nomeFase } from "./torneioFormato";

export default function TorneioChave({ lutas, totalFases, podeLancarResultado, onEscolherVencedor }) {
  const fases = Array.from({ length: totalFases }, (_, indice) => indice + 1);

  function renderEquipe(luta, equipe) {
    if (!equipe) {
      return <div className="chave-equipe chave-equipe-vazia">{luta.fase === 1 ? "Folga" : "A definir"}</div>;
    }

    const venceu = luta.vencedorId === equipe.id;
    const perdeu = luta.vencedorId && !venceu;

    return (
      <div className={`chave-equipe ${venceu ? "chave-vencedor" : ""} ${perdeu ? "chave-perdedor" : ""}`}>
        <div className="chave-equipe-nome">
          <strong>{equipe.nome}</strong>
          <small>{equipe.turma}</small>
        </div>
        {venceu && <span className="chave-selo">Venceu</span>}
        {podeLancarResultado && luta.apta && (
          <button className="button button-small button-light" onClick={() => onEscolherVencedor(luta, equipe)}>Vencedor</button>
        )}
      </div>
    );
  }

  return (
    <div className="chave">
      {fases.map((fase) => (
        <div key={fase} className="chave-fase">
          <h3>{nomeFase(fase, totalFases)}</h3>
          <div className="chave-lutas">
            {lutas.filter((luta) => luta.fase === fase).map((luta) => (
              <div key={luta.id} className="chave-luta">
                <span className="chave-luta-numero">Luta {luta.ordem}</span>
                {renderEquipe(luta, luta.equipeA)}
                {renderEquipe(luta, luta.equipeB)}
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
