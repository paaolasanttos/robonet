package br.com.aulas.service;

import br.com.aulas.dto.DashboardResumoResponse;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.AulasAlunoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final AulasAlunoRepository aulasAlunoRepository;
    private final JdbcTemplate jdbcTemplate;

    public DashboardService(AulasAlunoRepository aulasAlunoRepository, JdbcTemplate jdbcTemplate) {
        this.aulasAlunoRepository = aulasAlunoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public DashboardResumoResponse resumo(Usuario usuario) {
        boolean aluno = "aluno".equalsIgnoreCase(usuario.getPerfil());
        long aulasConcluidas = aluno
                ? aulasAlunoRepository.countByIdUsuarioAndConcluidaIgnoreCase(usuario.getId(), "S")
                : aulasAlunoRepository.countConcluidasPorAlunosAtivos();
        long[] resultadosSimulados = resultadosSimulados(usuario, aluno);
        long[] resultadosTorneios = resultadosTorneios(usuario, aluno);

        return new DashboardResumoResponse(
                aulasConcluidas,
                resultadosSimulados[0],
                resultadosSimulados[1],
                resultadosSimulados[2],
                resultadosTorneios[0],
                resultadosTorneios[1],
                resultadosTorneios[2]
        );
    }

    private long[] resultadosSimulados(Usuario usuario, boolean aluno) {
        String escopo = aluno
                ? "sr.idusuario = ?"
                : """
                  EXISTS (
                      SELECT 1
                        FROM usuarios u
                        JOIN usuariosturmas ut_aluno ON ut_aluno.idusuario = u.idusuario
                        JOIN usuariosturmas ut_professor ON ut_professor.idturma = ut_aluno.idturma
                       WHERE u.idusuario = sr.idusuario
                         AND LOWER(u.perfil) = 'aluno'
                         AND LOWER(u.status) = 'ativo'
                         AND ut_professor.idusuario = ?
                  )
                  """;

        return jdbcTemplate.queryForObject("""
                SELECT COUNT(DISTINCT (sr.idusuario, sr.idsimulado, sr.tentativa)),
                       COUNT(*) FILTER (WHERE sr.respostacorreta),
                       COUNT(*) FILTER (WHERE NOT sr.respostacorreta)
                  FROM simuladosrealizados sr
                 WHERE %s
                """.formatted(escopo),
                (result, rowNumber) -> new long[]{
                        result.getLong(1), result.getLong(2), result.getLong(3)
                },
                usuario.getId());
    }

    private long[] resultadosTorneios(Usuario usuario, boolean aluno) {
        String escopo = aluno
                ? "EXISTS (SELECT 1 FROM equipesmembros em WHERE em.idequipe = te.idequipe AND em.idusuario = ?)"
                : """
                  EXISTS (
                      SELECT 1
                        FROM equipesmembros em
                        JOIN usuarios u ON u.idusuario = em.idusuario
                        JOIN usuariosturmas ut_aluno ON ut_aluno.idusuario = u.idusuario
                        JOIN usuariosturmas ut_professor ON ut_professor.idturma = ut_aluno.idturma
                       WHERE em.idequipe = te.idequipe
                         AND LOWER(u.perfil) = 'aluno'
                         AND LOWER(u.status) = 'ativo'
                         AND ut_professor.idusuario = ?
                  )
                  """;

        return jdbcTemplate.queryForObject("""
                SELECT COUNT(DISTINCT te.idtorneio),
                       COUNT(DISTINCT CASE WHEN l.idvencedor = te.idequipe THEN l.idluta END),
                       COUNT(DISTINCT CASE
                           WHEN l.idvencedor IS NOT NULL AND l.idvencedor <> te.idequipe THEN l.idluta
                       END)
                  FROM torneiosequipes te
                  LEFT JOIN lutas l
                    ON l.idtorneio = te.idtorneio
                   AND (l.idequipea = te.idequipe OR l.idequipeb = te.idequipe)
                 WHERE %s
                """.formatted(escopo),
                (result, rowNumber) -> new long[]{result.getLong(1), result.getLong(2), result.getLong(3)},
                usuario.getId());
    }
}
