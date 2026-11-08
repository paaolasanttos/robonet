package br.com.aulas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lutas")
public class Luta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idluta")
    private Long id;

    @Column(name = "idtorneio", nullable = false)
    private Long idTorneio;

    @Column(name = "fase", nullable = false)
    private Integer fase;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @Column(name = "idequipea")
    private Long idEquipeA;

    @Column(name = "idequipeb")
    private Long idEquipeB;

    @Column(name = "idvencedor")
    private Long idVencedor;

    @Column(name = "dtresultado")
    private LocalDateTime dtResultado;

    public Luta() {
    }

    public Long getId() {
        return id;
    }

    public Long getIdTorneio() {
        return idTorneio;
    }

    public void setIdTorneio(Long idTorneio) {
        this.idTorneio = idTorneio;
    }

    public Integer getFase() {
        return fase;
    }

    public void setFase(Integer fase) {
        this.fase = fase;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Long getIdEquipeA() {
        return idEquipeA;
    }

    public void setIdEquipeA(Long idEquipeA) {
        this.idEquipeA = idEquipeA;
    }

    public Long getIdEquipeB() {
        return idEquipeB;
    }

    public void setIdEquipeB(Long idEquipeB) {
        this.idEquipeB = idEquipeB;
    }

    public Long getIdVencedor() {
        return idVencedor;
    }

    public void setIdVencedor(Long idVencedor) {
        this.idVencedor = idVencedor;
    }

    public LocalDateTime getDtResultado() {
        return dtResultado;
    }

    public void setDtResultado(LocalDateTime dtResultado) {
        this.dtResultado = dtResultado;
    }
}
