package br.com.aulas.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SimuladoRespostaRequest(
        @NotEmpty(message = "Responda às perguntas antes de enviar")
        List<@NotNull(message = "Uma resposta está inválida") Resposta> respostas
) {
    public record Resposta(
            @NotNull(message = "A pergunta é obrigatória") Integer perguntaId,
            @NotNull(message = "Selecione uma opção para cada pergunta") Integer opcaoId
    ) { }
}
