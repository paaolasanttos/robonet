package br.com.aulas.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "termosversoes")
public class TermoVersao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtermoversao")
    private Integer id;

    @Column(name = "versao", nullable = false, length = 20)
    private String versao;

    @Column(name = "dtversao", nullable = false)
    private LocalDate dtVersao;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "dtregistro", insertable = false, updatable = false)
    private LocalDateTime dtRegistro;

    public TermoVersao() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersao() {
        return versao;
    }

    public void setVersao(String versao) {
        this.versao = versao;
    }

    public LocalDate getDtVersao() {
        return dtVersao;
    }

    public void setDtVersao(LocalDate dtVersao) {
        this.dtVersao = dtVersao;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public LocalDateTime getDtRegistro() {
        return dtRegistro;
    }
}
