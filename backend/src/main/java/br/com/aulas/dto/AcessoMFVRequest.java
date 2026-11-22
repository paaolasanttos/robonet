package br.com.aulas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AcessoMFVRequest(
        @NotBlank(message = "Informe a senha atual")
        String senhaAtual,
        @NotBlank(message = "Informe se a verificação será ativada")
        @Pattern(regexp = "(?i)^[SN]$", message = "A verificação deve ser S ou N")
        String acessoMFV
) { }
