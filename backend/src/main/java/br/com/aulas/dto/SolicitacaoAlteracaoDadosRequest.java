package br.com.aulas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SolicitacaoAlteracaoDadosRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 2000, message = "O nome deve ter até 2000 caracteres")
        String nome,
        @Size(max = 50, message = "O RGM deve ter até 50 caracteres")
        String rgm,
        @NotBlank(message = "Informe o perfil")
        @Pattern(regexp = "(?i)^(aluno|professor)$", message = "O perfil deve ser aluno ou professor")
        String perfil,
        @Size(max = 2000, message = "A turma deve ter até 2000 caracteres")
        String turma
) { }
