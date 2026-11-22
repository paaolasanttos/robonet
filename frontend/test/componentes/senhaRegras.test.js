import assert from "node:assert/strict";
import test from "node:test";
import { MENSAGEM_SENHA, senhaValida } from "./senhaRegras.js";

test("senhaValida aceita senha que atende todos os requisitos", () => {
  assert.equal(senhaValida("Senha@123"), true);
  assert.equal(senhaValida("A1!bcdef"), true);
});

test("senhaValida rejeita senhas incompletas", () => {
  assert.equal(senhaValida(""), false);
  assert.equal(senhaValida("senha@123"), false);
  assert.equal(senhaValida("SENHA@123"), false);
  assert.equal(senhaValida("Senha1234"), false);
  assert.equal(senhaValida("S@1abc"), false);
});

test("mantem a mensagem padrao exibida ao usuario", () => {
  assert.equal(
    MENSAGEM_SENHA,
    "A senha ainda não atende a todos os requisitos.",
  );
});
