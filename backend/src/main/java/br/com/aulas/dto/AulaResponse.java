package br.com.aulas.dto;

public class AulaResponse {

    private Integer id;
    private Integer temaId;
    private String tema;
    private String titulo;
    private String descricao;

    public AulaResponse() {
    }

    public AulaResponse(Integer id, Integer temaId, String tema, String titulo, String descricao) {
        this.id = id;
        this.temaId = temaId;
        this.tema = tema;
        this.titulo = titulo;
        this.descricao = descricao;
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
}