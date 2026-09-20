package com.robonet.admin.dto;
import jakarta.validation.constraints.NotNull;

public class StatusRequest {
    @NotNull(message= "O campo 'ativo' é obrigatório")
    private Boolean ativo;

    public Boolean getAtivo() {
        return ativo;
    }
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
