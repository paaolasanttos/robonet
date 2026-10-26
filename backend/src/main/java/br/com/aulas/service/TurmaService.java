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

    public TurmaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TurmaResponse> listar() {
        return jdbcTemplate.query(
                "SELECT idturma, nome FROM turmas ORDER BY nome",
                (result, rowNumber) -> new TurmaResponse(result.getLong("idturma"), result.getString("nome"))
        );
    }

    @Transactional
    public TurmaResponse cadastrar(String nome) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO turmas (nome) VALUES (?) RETURNING idturma, nome",
                (result, rowNumber) -> new TurmaResponse(result.getLong("idturma"), result.getString("nome")),
                nome.trim()
        );
    }

    @Transactional
    public void excluir(Long idTurma) {
        if (jdbcTemplate.update("DELETE FROM turmas WHERE idturma = ?", idTurma) == 0) {
            throw new RegistroNaoEncontradoException("Turma não encontrada.");
        }
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
}
