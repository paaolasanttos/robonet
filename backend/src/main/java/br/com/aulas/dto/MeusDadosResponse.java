package br.com.aulas.dto;

import java.time.LocalDate;
import java.util.List;

public record MeusDadosResponse(String nome, String email, String perfil, String ra, String acessoMFV, List<String> turmas,
                                LocalDate exclusaoSolicitadaEm, LocalDate exclusaoAte,
                                List<TurmaResponse> turmasDisponiveis,
                                List<SolicitacaoAlteracaoResponse> solicitacoesAlteracao) {
}
