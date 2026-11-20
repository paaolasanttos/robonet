package br.com.aulas.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioListaResponse(Long id, String nome, String email, String ra, String perfil, Boolean ativo,
                                   String acessoMFV, LocalDateTime criadoEm, Long idTurma, List<Long> turmas,
                                   Boolean termoAceito) {
}
