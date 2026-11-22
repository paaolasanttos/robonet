package br.com.aulas.dto;

import java.time.LocalDateTime;

public record SolicitacaoCadastroResponse(Long id, String nome, String email, String ra, String perfil, String status,
                                          String motivo, LocalDateTime dtSolicitacao, LocalDateTime dtAnalise,
                                          String usrAnalise) {
}
