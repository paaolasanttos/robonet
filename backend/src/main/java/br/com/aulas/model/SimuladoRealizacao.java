package br.com.aulas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "simuladosrealizacoes")
public class SimuladoRealizacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idrealizacao")
    private Long id;

    @Column(name = "idusuario", nullable = false)
    private Long idUsuario;

    @Column(name = "idsimulado")
    private Integer idSimulado;

    @Column(name = "dtrealizacao", nullable = false)
    private LocalDateTime dataRealizacao;

    public SimuladoRealizacao() {
    }

    public SimuladoRealizacao(Long idUsuario, Integer idSimulado, LocalDateTime dataRealizacao) {
        this.idUsuario = idUsuario;
        this.idSimulado = idSimulado;
        this.dataRealizacao = dataRealizacao;
    }

    public Long getId() {
        return id;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public Integer getIdSimulado() {
        return idSimulado;
    }

    public LocalDateTime getDataRealizacao() {
        return dataRealizacao;
    }
}
