package com.robonet.admin.dto;
import java.time.LocalDateTime;

public class UsuarioResponse {

    private final Long id;
    private final String nome;
    private final String email;
    private final String rgm;
    private final String perfil;
    private final Boolean ativo;
    private final LocalDateTime criadoEm;

    public UsuarioResponse(long id, String nome, String email, String rgm, String perfil, Boolean ativo, LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.rgm = rgm;
        this.perfil = perfil;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }
    public String getNome() {
        return nome;
    }
    public String getEmail() {
        return email;
    }
    public String getRgm() {
        return rgm;
    }
    public String getPerfil() {
        return perfil;
    }
    public Boolean getAtivo() {
        return ativo;
    }
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}