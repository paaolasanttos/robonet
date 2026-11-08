package br.com.aulas.dto;

public record DashboardResumoResponse(
        long aulasConcluidas,
        long simuladosRealizados,
        long acertos,
        long erros,
        long vitorias,
        long torneiosDisputados,
        long derrotas
) {
}
