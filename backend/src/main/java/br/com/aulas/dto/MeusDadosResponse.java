package br.com.aulas.dto;

import java.util.List;

public record MeusDadosResponse(String nome, String email, String perfil, String rgm, List<String> turmas) {
}
