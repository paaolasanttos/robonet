package br.com.aulas.dto;

import java.util.List;

public record MinhaEquipeResponse(String nome, String turma, List<String> integrantes) {
}
