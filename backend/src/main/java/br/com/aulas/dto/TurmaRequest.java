package br.com.aulas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TurmaRequest(
        @NotBlank(message = "Informe o nome da turma")
        @Size(max = 120, message = "O nome da turma deve ter no máximo 120 caracteres")
        String nome
) { }
