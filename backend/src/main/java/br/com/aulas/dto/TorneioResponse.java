package br.com.aulas.dto;

import java.time.LocalDate;

public record TorneioResponse(Long id, String nome, String local, LocalDate dtInicio, LocalDate dtFim, String regras,
                              String status, String professor, long totalEquipes, boolean podeGerenciar) {
}
