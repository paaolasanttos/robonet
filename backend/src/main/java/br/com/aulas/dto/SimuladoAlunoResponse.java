package br.com.aulas.dto;

import br.com.aulas.model.Simulado;

import java.util.List;

public record SimuladoAlunoResponse(Integer id, Integer temaId, String tema, String titulo, List<PerguntaResponse> perguntas) {
    public static SimuladoAlunoResponse from(Simulado simulado) {
        return new SimuladoAlunoResponse(
                simulado.getId(), simulado.getTema().getId(), simulado.getTema().getTema(), simulado.getTitulo(),
                simulado.getPerguntas().stream().map(pergunta -> new PerguntaResponse(
                        pergunta.getId(), pergunta.getEnunciado(),
                        pergunta.getOpcoes().stream()
                                .map(opcao -> new OpcaoResponse(opcao.getId(), opcao.getTexto()))
                                .toList()
                )).toList()
        );
    }

    public record PerguntaResponse(Integer id, String enunciado, List<OpcaoResponse> opcoes) { }
    public record OpcaoResponse(Integer id, String texto) { }
}
