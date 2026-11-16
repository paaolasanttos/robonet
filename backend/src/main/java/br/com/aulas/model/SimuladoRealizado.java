package br.com.aulas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "simuladosrealizados")
public class SimuladoRealizado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idsimuladorealizado")
    private Integer id;

    @Column(name = "idsimulado", nullable = false)
    private Integer idSimulado;

    @Column(name = "idsimuladopergunta", nullable = false)
    private Integer idSimuladoPergunta;

    @Column(name = "idsimuladoopcao", nullable = false)
    private Integer idSimuladoOpcao;

    @Column(name = "respostacorreta", nullable = false)
    private boolean respostaCorreta;

    @Column(name = "idusuario", nullable = false)
    private Long idUsuario;

    @Column(name = "dttentativa", nullable = false)
    private LocalDateTime dataTentativa;

    @Column(name = "tentativa", nullable = false)
    private Integer tentativa;

    protected SimuladoRealizado() {
    }

    public SimuladoRealizado(Integer idSimulado, Integer idSimuladoPergunta, Integer idSimuladoOpcao,
                             boolean respostaCorreta, Long idUsuario, LocalDateTime dataTentativa, Integer tentativa) {
        this.idSimulado = idSimulado;
        this.idSimuladoPergunta = idSimuladoPergunta;
        this.idSimuladoOpcao = idSimuladoOpcao;
        this.respostaCorreta = respostaCorreta;
        this.idUsuario = idUsuario;
        this.dataTentativa = dataTentativa;
        this.tentativa = tentativa;
    }

    public Integer getIdSimulado() { return idSimulado; }
    public Integer getIdSimuladoPergunta() { return idSimuladoPergunta; }
    public boolean isRespostaCorreta() { return respostaCorreta; }
    public LocalDateTime getDataTentativa() { return dataTentativa; }
    public Integer getTentativa() { return tentativa; }
}
