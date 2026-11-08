package br.com.aulas.dto;

import java.time.LocalDateTime;

public record SolicitacaoAlteracaoAnaliseResponse(
        Long id,
        String email,
        String nome,
        String rgm,
        String perfil,
        String turma,
        String status,
        String motivo,
        LocalDateTime dtRegistro,
        LocalDateTime dtAnalise,
        String nomeAtual,
        String rgmAtual,
        String perfilAtual,
        String turmaAtual
) { }
