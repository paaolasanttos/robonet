package br.com.aulas.dto;

import java.time.LocalDateTime;

public record ExclusaoAnaliseResponse(Long id, Long idUsuario, String email, String status, LocalDateTime dtSolicitacao,
                                      LocalDateTime dtLimite, LocalDateTime dtConclusao, String usrAnalise) {
}
