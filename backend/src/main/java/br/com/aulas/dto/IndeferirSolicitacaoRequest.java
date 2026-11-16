package br.com.aulas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IndeferirSolicitacaoRequest(
        @NotBlank(message = "Informe o motivo do indeferimento")
        @Size(max = 2000, message = "O motivo deve ter até 2000 caracteres")
        String motivo
) { }
