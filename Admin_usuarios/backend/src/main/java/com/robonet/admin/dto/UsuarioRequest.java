package com.robonet.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public class UsuarioRequest {

    @NotBlank(message = "o nome é obrigatório")
    private String nome;

    @NotBlank(message="o email é obrigatório")
    @Email(message = "o email deve ser válido")
    private String email;

    private String rgm;

    @NotBlank(message="o perfil é obrigatório")
    private String perfil;

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getRgm() {return rgm;}
    public void setRgm(String rgm) {this.rgm = rgm;}

    public String getPerfil() {return perfil;}
    public void setPerfil(String perfil) {this.perfil = perfil;}

}
