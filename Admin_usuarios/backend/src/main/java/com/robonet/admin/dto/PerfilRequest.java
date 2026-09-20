package com.robonet.admin.dto;
import jakarta.validation.constraints.NotBlank;

public class PerfilRequest {
    @NotBlank(message = "o nome do perfil é obrigatório")
    private String perfil;

    public String getPerfil() {
        return perfil;
    }
    public void setPerfil(String perfil) {this.perfil = perfil;}
}
