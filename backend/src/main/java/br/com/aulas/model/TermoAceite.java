package br.com.aulas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "termosaceites")
public class TermoAceite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtermoaceite")
    private Long id;

    @Column(name = "idusuario", nullable = false)
    private Long idUsuario;

    @Column(name = "versao", nullable = false, length = 20)
    private String versao;

    @Column(name = "ip", length = 100)
    private String ip;

    @Column(name = "dtaceite", nullable = false)
    private LocalDateTime dtAceite;

    public TermoAceite() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getVersao() {
        return versao;
    }

    public void setVersao(String versao) {
        this.versao = versao;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LocalDateTime getDtAceite() {
        return dtAceite;
    }

    public void setDtAceite(LocalDateTime dtAceite) {
        this.dtAceite = dtAceite;
    }
}
