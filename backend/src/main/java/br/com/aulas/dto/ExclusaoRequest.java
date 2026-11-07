package br.com.aulas.dto;

import jakarta.validation.constraints.NotBlank;

public record ExclusaoRequest(
        @NotBlank(message = "Informe a senha atual")
        String senhaAtual
) { }
