package br.com.aulas.dto;

public class PasswordResetRequest {
    private String email;
    private String codigo;
    private String senha;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}