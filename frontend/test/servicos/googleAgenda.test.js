import assert from "node:assert/strict";
import test from "node:test";
import { aindaVaiAcontecer, hoje, linkGoogleAgenda } from "../../src/servicos/googleAgenda.js";

function deslocarDia(data, quantidade) {
  const resultado = new Date(`${data}T00:00:00Z`);
  resultado.setUTCDate(resultado.getUTCDate() + quantidade);
  return resultado.toISOString().slice(0, 10);
}

test("cria link do Google Agenda com data final exclusiva para torneio de vários dias", () => {
  const url = new URL(linkGoogleAgenda(
    { nome: "Final Robótica", dtInicio: "2026-12-31", dtFim: "2027-01-02", local: "Mogi das Cruzes" },
    { nome: "Equipe & Robôs", turma: "Turma A" },
  ));

  assert.equal(url.origin, "https://calendar.google.com");
  assert.equal(url.searchParams.get("action"), "TEMPLATE");
  assert.equal(url.searchParams.get("text"), "Torneio Final Robótica");
  assert.equal(url.searchParams.get("dates"), "20261231/20270103");
  assert.equal(url.searchParams.get("details"), "Torneio de robótica da equipe Equipe & Robôs (Turma A) - Robonet");
  assert.equal(url.searchParams.get("location"), "Mogi das Cruzes");
});

test("usa o dia seguinte como fim exclusivo e omite local não informado", () => {
  const url = new URL(linkGoogleAgenda(
    { nome: "Torneio", dtInicio: "2026-03-08" },
    { nome: "Equipe", turma: "Turma B" },
  ));

  assert.equal(url.searchParams.get("dates"), "20260308/20260309");
  assert.equal(url.searchParams.has("location"), false);
});

test("rejeita data inválida em vez de gerar um evento incorreto", () => {
  assert.throws(
    () => linkGoogleAgenda(
      { nome: "Torneio", dtInicio: "2026-02-30" },
      { nome: "Equipe", turma: "Turma B" },
    ),
    RangeError,
  );
});

test("mantém a opção de agenda para torneios em andamento que ainda não terminaram", () => {
  const dataAtual = hoje();
  const inicio = deslocarDia(dataAtual, -4);
  const fim = deslocarDia(dataAtual, 3);

  assert.equal(aindaVaiAcontecer({ status: "Andamento", dtInicio: inicio, dtFim: fim }), true);
  assert.equal(aindaVaiAcontecer({ status: "Inscrição", dtInicio: inicio, dtFim: deslocarDia(dataAtual, -1) }), false);
  assert.equal(aindaVaiAcontecer({ status: "Finalizado", dtInicio: dataAtual, dtFim: fim }), false);
});
