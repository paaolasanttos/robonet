package br.com.aulas.dto;

import jakarta.validation.constraints.NotBlank;

public record EmailConfirmacaoRequest(
        @NotBlank(message = "Informe o novo e-mail")
        String novoEmail,
        @NotBlank(message = "Informe o código recebido")
        String codigo
) { }
