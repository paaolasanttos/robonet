package br.com.aulas.dto;

import java.time.LocalDate;
import java.util.List;

public record MinhaEquipeResponse(String nome, String turma, List<String> integrantes, List<Torneio> torneios) {

    public record Torneio(Long id, String nome, String local, LocalDate dtInicio, LocalDate dtFim, String status) {
    }
}
