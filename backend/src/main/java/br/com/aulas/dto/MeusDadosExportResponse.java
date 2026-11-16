package br.com.aulas.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MeusDadosExportResponse(LocalDateTime geradoEm, String nome, String email, String perfil, String ra,
                                      String status, String codigoPorEmail, LocalDateTime cadastradoEm,
                                      List<String> turmas, String equipe, List<String> aulasConcluidas,
                                      List<SimuladoRealizado> simuladosRealizados,
                                      List<ResultadoSimulado> resultadosSimulados,
                                      List<RespostaSimulado> respostasSimulados,
                                      List<SolicitacaoAlteracao> solicitacoesAlteracao,
                                      List<SolicitacaoExclusao> solicitacoesExclusao,
                                      List<AceiteTermo> aceitesTermo, List<LocalDateTime> acessos,
                                      List<RegistroAuditoria> registrosAuditoria) {
    public record SimuladoRealizado(String simulado, LocalDateTime data) {}

    public record ResultadoSimulado(String simulado, Integer tentativa, Integer acertos, Integer total, LocalDateTime data) {}

    public record RespostaSimulado(String simulado, Integer tentativa, String pergunta, String resposta, Boolean correta,
                                   LocalDateTime data) {}

    public record SolicitacaoAlteracao(String nome, String email, String ra, String perfil, String turma, String status,
                                       String motivo, LocalDateTime dataSolicitacao, LocalDateTime dataAnalise) {}

    public record SolicitacaoExclusao(String status, String ip, LocalDateTime dataSolicitacao, LocalDateTime dataLimite,
                                      LocalDateTime dataConclusao) {}

    public record AceiteTermo(String versao, String ip, LocalDateTime data) {}

    public record RegistroAuditoria(String categoria, String acao, String descricao, String ip, Boolean sucesso,
                                    LocalDateTime data) {}
}
