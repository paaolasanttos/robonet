package br.com.aulas.service;

import br.com.aulas.dto.TurmaResponse;
import br.com.aulas.exception.RegistroNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class TurmaService {
    private final JdbcTemplate jdbcTemplate;
    private final AuditoriaService auditoriaService;

    public TurmaService(JdbcTemplate jdbcTemplate, AuditoriaService auditoriaService) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditoriaService = auditoriaService;
    }

    public List<TurmaResponse> listar() {
        return jdbcTemplate.query(
                "SELECT idturma, nome FROM turmas ORDER BY nome",
                (result, rowNumber) -> new TurmaResponse(result.getLong("idturma"), result.getString("nome"))
        );
    }

    @Transactional
    public TurmaResponse cadastrar(String nome) {
        TurmaResponse turma = jdbcTemplate.queryForObject(
                "INSERT INTO turmas (nome) VALUES (?) RETURNING idturma, nome",
                (result, rowNumber) -> new TurmaResponse(result.getLong("idturma"), result.getString("nome")),
                nome.trim()
        );
        auditoriaService.registrar("TURMAS", "TURMA_CRIADA", "Turma " + turma.id() + " - " + turma.nome(), true);
        return turma;
    }

    @Transactional
    public void excluir(Long idTurma) {
        String nome = jdbcTemplate.queryForList("SELECT nome FROM turmas WHERE idturma = ?", String.class, idTurma)
                .stream().findFirst().orElse(null);
        if (jdbcTemplate.update("DELETE FROM turmas WHERE idturma = ?", idTurma) == 0) {
            throw new RegistroNaoEncontradoException("Turma não encontrada.");
        }
        auditoriaService.registrar("TURMAS", "TURMA_EXCLUIDA", "Turma " + idTurma + " - " + nome, true);
    }

    public void validarTurma(Long idTurma) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM turmas WHERE idturma = ?)",
                Boolean.class,
                idTurma
        );
        if (!Boolean.TRUE.equals(exists)) {
            throw new RegistroNaoEncontradoException("Turma não encontrada.");
        }
    }

    @Transactional
    public void salvarTurmasUsuario(Long idUsuario, List<Long> idTurmas) {
        List<Long> turmas = idTurmas == null
                ? null
                : idTurmas.stream().filter(Objects::nonNull).distinct().toList();
        if (turmas != null) turmas.forEach(this::validarTurma);

        jdbcTemplate.update("DELETE FROM usuariosturmas WHERE idusuario = ?", idUsuario);
        if (turmas == null) {
            return;
        }

        turmas.forEach(idTurma ->
                jdbcTemplate.update(
                        "INSERT INTO usuariosturmas (idusuario, idturma) VALUES (?, ?)",
                        idUsuario,
                        idTurma
                )
        );
    }

    public Long turmaDoAluno(Long idUsuario) {
        return jdbcTemplate.queryForList(
                "SELECT idturma FROM usuariosturmas WHERE idusuario = ? ORDER BY idusuarioturma LIMIT 1",
                Long.class,
                idUsuario
        ).stream().findFirst().orElse(null);
    }

    public List<Long> idsTurmasDoUsuario(Long idUsuario) {
        return jdbcTemplate.queryForList(
                "SELECT idturma FROM usuariosturmas WHERE idusuario = ? ORDER BY idturma",
                Long.class,
                idUsuario
        );
    }

    public List<Long> idsUsuariosDaTurma(Long idTurma) {
        return jdbcTemplate.queryForList(
                "SELECT idusuario FROM usuariosturmas WHERE idturma = ?",
                Long.class,
                idTurma
        );
    }

    public List<TurmaResponse> listarDoUsuario(String email) {
        return jdbcTemplate.query(
                "SELECT t.idturma, t.nome FROM turmas t "
                        + "JOIN usuariosturmas ut ON ut.idturma = t.idturma "
                        + "JOIN usuarios u ON u.idusuario = ut.idusuario "
                        + "WHERE LOWER(u.email) = LOWER(?) AND u.status = 'Ativo' ORDER BY t.nome",
                (result, rowNumber) -> new TurmaResponse(result.getLong("idturma"), result.getString("nome")),
                email
        );
    }
}
