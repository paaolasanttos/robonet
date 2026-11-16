package br.com.aulas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "aulastemas")
public class AulaTema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idaulatema")
    private Integer id;

    @Column(name = "descricao", nullable = false, length = 120)
    private String tema;

    public AulaTema() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }
}