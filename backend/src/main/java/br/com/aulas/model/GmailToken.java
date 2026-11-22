package br.com.aulas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "gmailtokens")
public class GmailToken {
    @Id
    private Long id;

    @Column(name = "refreshtoken", nullable = false, length = 2048)
    private String refreshToken;

    @Column(name = "dtatualizacao", nullable = false)
    private LocalDateTime dtAtualizacao;

    protected GmailToken() {
    }

    public GmailToken(Long id, String refreshToken, LocalDateTime dtAtualizacao) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.dtAtualizacao = dtAtualizacao;
    }

    public Long getId() {
        return id;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public LocalDateTime getDtAtualizacao() {
        return dtAtualizacao;
    }
}
