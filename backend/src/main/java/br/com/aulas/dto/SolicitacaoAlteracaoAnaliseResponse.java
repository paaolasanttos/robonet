package br.com.aulas.dto;

import java.time.LocalDateTime;

public record SolicitacaoAlteracaoAnaliseResponse(
        Long id,
        String email,
        String nome,
        String ra,
        String perfil,
        String turma,
        String status,
        String motivo,
        LocalDateTime dtRegistro,
        LocalDateTime dtAnalise,
        String nomeAtual,
        String raAtual,
        String perfilAtual,
        String turmaAtual
) { }
