package br.com.aulas.dto;

public class AulaResponse {

    private Integer id;
    private Integer temaId;
    private String tema;
    private String titulo;
    private String descricao;
    private Boolean concluida;

    public AulaResponse() {
    }

    public AulaResponse(Integer id, Integer temaId, String tema, String titulo, String descricao) {
        this(id, temaId, tema, titulo, descricao, false);
    }

    public AulaResponse(Integer id, Integer temaId, String tema, String titulo, String descricao, Boolean concluida) {
        this.id = id;
        this.temaId = temaId;
        this.tema = tema;
        this.titulo = titulo;
        this.descricao = descricao;
        this.concluida = concluida != null ? concluida : false;
    }

    public Integer getId() {
        return id;
    }

    public Integer getTemaId() {
        return temaId;
    }

    public String getTema() {
        return tema;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Boolean getConcluida() {
        return concluida != null && concluida;
    }

    public void setConcluida(Boolean concluida) {
        this.concluida = concluida != null ? concluida : false;
    }
}