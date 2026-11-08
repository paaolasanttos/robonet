package br.com.aulas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class EquipeRequest {

    @NotBlank(message = "O nome da equipe é obrigatório")
    @Size(max = 120, message = "O nome da equipe deve ter no máximo 120 caracteres")
    private String nome;

    @NotNull(message = "A turma é obrigatória")
    private Long turmaId;

    @NotNull(message = "Selecione os integrantes da equipe")
    @Size(min = 1, max = 4, message = "A equipe deve ter de 1 a 4 integrantes")
    private List<Long> membros;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }

    public List<Long> getMembros() {
        return membros;
    }

    public void setMembros(List<Long> membros) {
        this.membros = membros;
    }
}
