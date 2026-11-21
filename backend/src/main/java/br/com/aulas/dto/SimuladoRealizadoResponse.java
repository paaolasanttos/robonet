package br.com.aulas.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SimuladoRealizadoResponse(
        Integer simuladoId,
        String tema,
        String titulo,
        Integer tentativa,
        LocalDateTime dataRealizacao,
        int acertos,
        int total,
        double percentual,
        List<PerguntaResponse> perguntas
) {
    public record PerguntaResponse(Integer id, String enunciado, boolean correta, List<OpcaoResponse> opcoes) { }
    public record OpcaoResponse(Integer id, String texto, boolean correta, boolean selecionada) { }
}
