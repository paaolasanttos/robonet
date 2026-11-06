package br.com.aulas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "aulasaluno")
public class AulasAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idaulaaluno")
    private Long idAulaAluno;

    @Column(name = "idaula", nullable = false)
    private Long idAula;

    @Column(name = "idaulatema", nullable = false)
    private Integer idAulaTema;

    @Column(name = "idusuario", nullable = false)
    private Long idUsuario;

    @Column(name = "concluida", nullable = false, length = 1)
    private String concluida;

    public AulasAluno() {
    }

    public Long getIdAulaAluno() {
        return idAulaAluno;
    }

    public void setIdAulaAluno(Long idAulaAluno) {
        this.idAulaAluno = idAulaAluno;
    }

    public Long getIdAula() {
        return idAula;
    }

    public void setIdAula(Long idAula) {
        this.idAula = idAula;
    }

    public Integer getIdAulaTema() {
        return idAulaTema;
    }

    public void setIdAulaTema(Integer idAulaTema) {
        this.idAulaTema = idAulaTema;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getConcluida() {
        return concluida;
    }

    public void setConcluida(String concluida) {
        this.concluida = concluida;
    }
}
