package br.com.aulas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "logcodigoverificacao")
public class LogCodigoVerificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idlogcodigoverificacao")
    private Long id;

    @Column(name = "email", nullable = false, length = 2000)
    private String email;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "dtgeracao", nullable = false)
    private LocalDateTime dtGeracao;

    public LogCodigoVerificacao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getDtGeracao() {
        return dtGeracao;
    }

    public void setDtGeracao(LocalDateTime dtGeracao) {
        this.dtGeracao = dtGeracao;
    }
}
