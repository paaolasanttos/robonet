package br.com.aulas.dto;

import java.time.LocalDate;

public record TermoAtualResponse(String versao, LocalDate dtVersao, String conteudo) {
}
