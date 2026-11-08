package br.com.aulas.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MeusDadosExportResponse(LocalDateTime geradoEm, String nome, String email, String perfil, String rgm,
                                      List<String> turmas, String equipe, List<String> aulasConcluidas,
                                      List<SimuladoRealizado> simuladosRealizados, List<AceiteTermo> aceitesTermo,
                                      List<LocalDateTime> acessos) {
    public record SimuladoRealizado(String simulado, LocalDateTime data) {}

    public record AceiteTermo(String versao, String ip, LocalDateTime data) {}
}
