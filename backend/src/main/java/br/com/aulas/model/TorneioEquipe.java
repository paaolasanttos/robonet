package br.com.aulas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "torneiosequipes")
public class TorneioEquipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtorneioequipe")
    private Long id;

    @Column(name = "idtorneio", nullable = false)
    private Long idTorneio;

    @Column(name = "idequipe", nullable = false)
    private Long idEquipe;

    public TorneioEquipe() {
    }

    public TorneioEquipe(Long idTorneio, Long idEquipe) {
        this.idTorneio = idTorneio;
        this.idEquipe = idEquipe;
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

    public Long getIdEquipe() {
        return idEquipe;
    }

    public void setIdEquipe(Long idEquipe) {
        this.idEquipe = idEquipe;
    }
}
