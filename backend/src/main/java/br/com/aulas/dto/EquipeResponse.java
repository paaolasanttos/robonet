package br.com.aulas.dto;

import java.util.List;

public record EquipeResponse(Long id, String nome, Long turmaId, String turma, List<Membro> membros, boolean emTorneio) {
    public record Membro(Long id, String nome) {}
}
