import assert from "node:assert/strict";
import test from "node:test";
import { formatarDataHoraAuditoria, normalizarDataHoraAuditoria } from "../../src/servicos/dataHoraAuditoria.js";

test("interpreta horários de auditoria sem fuso como UTC", () => {
  const data = normalizarDataHoraAuditoria("2026-11-22T21:47:47");

  assert.equal(data, "2026-11-22T21:47:47Z");
  assert.equal(new Date(data).toISOString(), "2026-11-22T21:47:47.000Z");
});

test("preserva horários de auditoria que já informam o fuso", () => {
  const data = "2026-11-22T21:47:47-03:00";

  assert.equal(normalizarDataHoraAuditoria(data), data);
  assert.equal(normalizarDataHoraAuditoria(null), null);
});

test("exibe horário UTC no fuso de São Paulo", () => {
  assert.equal(formatarDataHoraAuditoria("2026-11-22T21:58:19"), "22/11/2026, 18:58:19");
});
