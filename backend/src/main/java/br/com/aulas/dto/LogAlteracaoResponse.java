package br.com.aulas.dto;

import java.time.LocalDateTime;

public record LogAlteracaoResponse(Long id, String tabela, Long idRegistro, String operacao, String campo,
                                   String valorAnterior, String valorNovo, String usuario, LocalDateTime dtAlteracao) {
}
