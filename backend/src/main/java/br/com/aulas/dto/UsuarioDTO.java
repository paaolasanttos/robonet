package br.com.aulas.dto;

import java.util.List;

public class UsuarioDTO {
    private String nome;
    private String email;
    private String rgm;
    private String perfil;
    private String senha;
    private Long turmaId;
    private List<Long> turmas;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRgm() { return rgm; }
    public void setRgm(String rgm) { this.rgm = rgm; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Long getTurmaId() { return turmaId; }
    public void setTurmaId(Long turmaId) { this.turmaId = turmaId; }

    public List<Long> getTurmas() { return turmas; }
    public void setTurmas(List<Long> turmas) { this.turmas = turmas; }
}
