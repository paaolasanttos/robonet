import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import TorneioLista from "../componentes/torneioLista";
import TorneioDetalhe from "../componentes/torneioDetalhe";
import TorneioEquipes from "../componentes/torneioEquipes";
import TorneioMinhaEquipe from "../componentes/torneioMinhaEquipe";
import MenuPadrao from "../componentes/menuPadrao";
import RodapePadrao from "../componentes/rodapePadrao";

export default function TorneiosPagina() {
  const { session } = useAuth();
  const perfil = String(session?.usuario?.perfil || "").toLowerCase();
  const [aba, setAba] = useState("torneios");
  const [torneioId, setTorneioId] = useState(null);

  function trocarAba(novaAba) {
    setAba(novaAba);
    setTorneioId(null);
  }

  return (
    <main className="app-shell">
      <MenuPadrao />

      <section className="page">
        <header className="page-header">
          <div>
            <span className="eyebrow">ROBONET</span>
            <h1>Torneios</h1>
            <p>{perfil === "aluno" ? "Acompanhe o chaveamento e os vencedores das lutas." : "Organize as equipes e os torneios no formato mata-mata."}</p>
          </div>
        </header>

        <div className="abas" role="tablist" aria-label="Seções de torneios">
          <button className={`aba ${aba === "torneios" ? "aba-ativa" : ""}`} role="tab" aria-selected={aba === "torneios"} onClick={() => trocarAba("torneios")}>Torneios</button>
          <button className={`aba ${aba === "equipes" ? "aba-ativa" : ""}`} role="tab" aria-selected={aba === "equipes"} onClick={() => trocarAba("equipes")}>{perfil === "aluno" ? "Minha equipe" : "Equipes"}</button>
        </div>

        {aba === "equipes" ? (
          perfil === "aluno" ? <TorneioMinhaEquipe /> : <TorneioEquipes perfil={perfil} />
        ) : torneioId ? (
          <TorneioDetalhe id={torneioId} perfil={perfil} onVoltar={() => setTorneioId(null)} />
        ) : (
          <TorneioLista perfil={perfil} onAbrir={setTorneioId} />
        )}
      </section>
      <RodapePadrao />
    </main>
  );
}
