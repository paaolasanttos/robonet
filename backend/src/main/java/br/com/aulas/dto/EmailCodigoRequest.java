package br.com.aulas.dto;

import jakarta.validation.constraints.NotBlank;

public record EmailCodigoRequest(
        @NotBlank(message = "Informe o novo e-mail")
        String novoEmail,
        @NotBlank(message = "Informe a senha atual")
        String senhaAtual
) { }
