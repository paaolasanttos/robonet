import AulaListaAluno from "../componentes/aulaListaAluno";
import MenuPadrao from "../componentes/menuPadrao";
import RodapePadrao from "../componentes/rodapePadrao";

export default function AulasAlunoPagina() {
  return (
    <main className="app-shell">
      <MenuPadrao />

      <section className="page">
        <AulaListaAluno />
      </section>
      <RodapePadrao />
    </main>
  );
}
