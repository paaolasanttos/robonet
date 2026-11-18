package br.com.aulas.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoesexclusao")
public class SolicitacaoExclusao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idsolicitacaoexclusao")
    private Long id;

    @Column(name = "idusuario", nullable = false)
    private Long idUsuario;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "ip")
    private String ip;

    @Column(name = "dtsolicitacao", nullable = false)
    private LocalDateTime dtSolicitacao;

    @Column(name = "dtlimite", nullable = false)
    private LocalDateTime dtLimite;

    @Column(name = "dtconclusao")
    private LocalDateTime dtConclusao;

    public Long getId() {
        return id;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LocalDateTime getDtSolicitacao() {
        return dtSolicitacao;
    }

    public void setDtSolicitacao(LocalDateTime dtSolicitacao) {
        this.dtSolicitacao = dtSolicitacao;
    }

    public LocalDateTime getDtLimite() {
        return dtLimite;
    }

    public void setDtLimite(LocalDateTime dtLimite) {
        this.dtLimite = dtLimite;
    }

    public LocalDateTime getDtConclusao() {
        return dtConclusao;
    }

    public void setDtConclusao(LocalDateTime dtConclusao) {
        this.dtConclusao = dtConclusao;
    }
}
