package br.com.aulas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SimuladoRequest {
    @NotNull(message = "O tema é obrigatório")
    private Integer temaId;

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 180, message = "O título deve ter no máximo 180 caracteres")
    private String titulo;

    @NotEmpty(message = "Cadastre ao menos uma pergunta")
    @Valid
    private List<@NotNull(message = "Uma pergunta está inválida") @Valid PerguntaRequest> perguntas;

    public Integer getTemaId() { return temaId; }
    public void setTemaId(Integer temaId) { this.temaId = temaId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public List<PerguntaRequest> getPerguntas() { return perguntas; }
    public void setPerguntas(List<PerguntaRequest> perguntas) { this.perguntas = perguntas; }

    public static class PerguntaRequest {
        @NotBlank(message = "O enunciado da pergunta é obrigatório")
        private String enunciado;

        @NotEmpty(message = "Cada pergunta deve ter ao menos duas opções")
        @Size(min = 2, message = "Cada pergunta deve ter ao menos duas opções")
        @Valid
        private List<@NotNull(message = "Uma opção está inválida") @Valid OpcaoRequest> opcoes;

        public String getEnunciado() { return enunciado; }
        public void setEnunciado(String enunciado) { this.enunciado = enunciado; }
        public List<OpcaoRequest> getOpcoes() { return opcoes; }
        public void setOpcoes(List<OpcaoRequest> opcoes) { this.opcoes = opcoes; }
    }

    public static class OpcaoRequest {
        @NotBlank(message = "O texto da opção é obrigatório")
        @Size(max = 500, message = "A opção deve ter no máximo 500 caracteres")
        private String texto;
        private boolean correta;

        public String getTexto() { return texto; }
        public void setTexto(String texto) { this.texto = texto; }
        public boolean isCorreta() { return correta; }
        public void setCorreta(boolean correta) { this.correta = correta; }
    }
}
