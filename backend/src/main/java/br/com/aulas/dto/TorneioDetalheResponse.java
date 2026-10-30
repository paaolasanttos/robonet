package br.com.aulas.dto;

import java.util.List;

public record TorneioDetalheResponse(TorneioResponse torneio, List<EquipeTorneio> equipes, List<LutaResponse> lutas,
                                     List<Posicao> ranking, int totalFases, boolean podeLancarResultado) {
    public record EquipeTorneio(Long id, String nome, String turma) {}

    public record LutaResponse(Long id, int fase, int ordem, EquipeTorneio equipeA, EquipeTorneio equipeB,
                               Long vencedorId, boolean apta) {}

    public record Posicao(int posicao, Long equipeId, String equipe, String turma) {}
}
