package br.com.aulas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TorneioRequest {

    @NotBlank(message = "O nome do torneio é obrigatório")
    @Size(max = 180, message = "O nome do torneio deve ter no máximo 180 caracteres")
    private String nome;

    @Size(max = 180, message = "O local deve ter no máximo 180 caracteres")
    private String local;

    @NotNull(message = "A data de início é obrigatória")
    private LocalDate dtInicio;

    private LocalDate dtFim;

    private String regras;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public LocalDate getDtInicio() {
        return dtInicio;
    }

    public void setDtInicio(LocalDate dtInicio) {
        this.dtInicio = dtInicio;
    }

    public LocalDate getDtFim() {
        return dtFim;
    }

    public void setDtFim(LocalDate dtFim) {
        this.dtFim = dtFim;
    }

    public String getRegras() {
        return regras;
    }

    public void setRegras(String regras) {
        this.regras = regras;
    }
}
