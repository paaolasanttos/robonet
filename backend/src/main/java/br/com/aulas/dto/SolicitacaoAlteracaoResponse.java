package br.com.aulas.dto;

import java.time.LocalDateTime;

public record SolicitacaoAlteracaoResponse(Long id, String nome, String ra, String perfil, String turma,
                                           String status, String motivo, LocalDateTime dtRegistro) {
}
