package br.com.aulas.dto;

import br.com.aulas.security.SenhaPolitica;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SenhaRequest(
        @NotBlank(message = "Informe a senha atual")
        String senhaAtual,
        @NotBlank(message = "Informe a nova senha")
        @Pattern(regexp = SenhaPolitica.REGEX, message = SenhaPolitica.MENSAGEM)
        String novaSenha
) { }
