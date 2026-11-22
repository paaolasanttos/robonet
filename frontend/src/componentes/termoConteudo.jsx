import { useEffect, useState } from "react";
import { termoApi } from "../servicos/api";

export function idTitulo(titulo) {
  return titulo
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-|-$/g, "");
}

export function formatarDataTermo(data) {
  if (!data) return "";
  return new Date(`${data}T00:00:00`).toLocaleDateString("pt-BR", { day: "numeric", month: "long", year: "numeric" });
}

export function titulosTermo(conteudo) {
  return (conteudo || "")
    .split("\n")
    .filter((linha) => /^#\s/.test(linha))
    .map((linha) => {
      const titulo = linha.replace(/^#\s+/, "").trim();
      return { id: idTitulo(titulo), titulo };
    });
}

export function useTermoAtual() {
  const [termo, setTermo] = useState(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    let ativo = true;
    termoApi.atual()
      .then((dados) => { if (ativo) setTermo(dados); })
      .catch((err) => { if (ativo) setErro(err.message || "Não foi possível carregar o termo."); });
    return () => { ativo = false; };
  }, []);

  return { termo, erro, setTermo };
}

function montarBlocos(conteudo) {
  const blocos = [];
  let secao = null;

  (conteudo || "").replace(/\r\n/g, "\n").split(/\n\s*\n/).map((bloco) => bloco.trim()).filter(Boolean).forEach((bloco) => {
    if (/^##\s/.test(bloco)) {
      secao = { tipo: "secao", titulo: bloco.replace(/^##\s+/, ""), paragrafos: [] };
      blocos.push(secao);
    } else if (/^#\s/.test(bloco)) {
      secao = null;
      const titulo = bloco.replace(/^#\s+/, "");
      blocos.push({ tipo: "titulo", titulo, id: idTitulo(titulo) });
    } else if (secao) {
      secao.paragrafos.push(bloco);
    } else {
      blocos.push({ tipo: "paragrafo", texto: bloco });
    }
  });

  return blocos;
}

export default function TermoConteudo({ termo, erro }) {
  if (erro) return <p className="login-error" role="alert">{erro}</p>;
  if (!termo) return <p className="meus-dados-ajuda">Carregando termo...</p>;

  return (
    <div className="termo-conteudo">
      {montarBlocos(termo.conteudo).map((bloco, indice) => {
        if (bloco.tipo === "titulo") return <h2 key={indice} id={bloco.id}>{bloco.titulo}</h2>;
        if (bloco.tipo === "paragrafo") return <p key={indice}>{bloco.texto}</p>;
        return (
          <section key={indice}>
            <h3>{bloco.titulo}</h3>
            {bloco.paragrafos.map((paragrafo, posicao) => <p key={posicao}>{paragrafo}</p>)}
          </section>
        );
      })}
    </div>
  );
}
