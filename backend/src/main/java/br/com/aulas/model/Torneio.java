package br.com.aulas.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "torneios")
public class Torneio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtorneio")
    private Long id;

    @Column(name = "nome", nullable = false, length = 180)
    private String nome;

    @Column(name = "local", length = 180)
    private String local;

    @Column(name = "dtinicio", nullable = false)
    private LocalDate dtInicio;

    @Column(name = "dtfim")
    private LocalDate dtFim;

    @Column(name = "regras")
    private String regras;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "idprofessor", nullable = false)
    private Long idProfessor;

    @Column(name = "dtregistro", insertable = false, updatable = false)
    private LocalDateTime dtRegistro;

    public Torneio() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public LocalDate getDtInicio() {
        return dtInicio;
    }

    public void setDtInicio(LocalDate dtInicio) {
        this.dtInicio = dtInicio;
    }

    public LocalDate getDtFim() {
        return dtFim;
    }

    public void setDtFim(LocalDate dtFim) {
        this.dtFim = dtFim;
    }

    public String getRegras() {
        return regras;
    }

    public void setRegras(String regras) {
        this.regras = regras;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getIdProfessor() {
        return idProfessor;
    }

    public void setIdProfessor(Long idProfessor) {
        this.idProfessor = idProfessor;
    }

    public LocalDateTime getDtRegistro() {
        return dtRegistro;
    }
}
