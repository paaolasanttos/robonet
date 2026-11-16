package br.com.aulas.service;

import br.com.aulas.dto.TurmaRequest;
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
    public TurmaResponse cadastrar(TurmaRequest dados) {
        List<Long> professores = idsUnicos(dados.professores());
        List<Long> alunos = idsUnicos(dados.alunos());
        professores.forEach(idProfessor -> validarUsuarioAtivo(idProfessor, "professor"));
        alunos.forEach(idAluno -> validarAlunoSemEquipe(validarUsuarioAtivo(idAluno, "aluno"), idAluno));

        TurmaResponse turma = jdbcTemplate.queryForObject(
                "INSERT INTO turmas (nome) VALUES (?) RETURNING idturma, nome",
                (result, rowNumber) -> new TurmaResponse(result.getLong("idturma"), result.getString("nome")),
                dados.nome().trim()
        );

        professores.forEach(idProfessor ->
                jdbcTemplate.update(
                        "INSERT INTO usuariosturmas (idusuario, idturma) VALUES (?, ?)",
                        idProfessor,
                        turma.id()
                )
        );
        alunos.forEach(idAluno -> salvarTurmasUsuario(idAluno, List.of(turma.id())));

        auditoriaService.registrar("TURMAS", "TURMA_CRIADA", "Turma " + turma.id() + " - " + turma.nome()
                + " com " + professores.size() + " professor(es) e " + alunos.size() + " aluno(s)", true);
        return turma;
    }

    private List<Long> idsUnicos(List<Long> ids) {
        return ids == null ? List.of() : ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private String validarUsuarioAtivo(Long idUsuario, String perfil) {
        return jdbcTemplate.queryForList(
                "SELECT nome FROM usuarios WHERE idusuario = ? AND LOWER(perfil) = ? AND status = 'Ativo'",
                String.class,
                idUsuario,
                perfil
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException(
                "Selecione apenas " + ("aluno".equals(perfil) ? "alunos" : "professores") + " ativos."));
    }

    private void validarAlunoSemEquipe(String nome, Long idAluno) {
        Boolean emEquipe = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM equipesmembros WHERE idusuario = ?)",
                Boolean.class,
                idAluno
        );
        if (Boolean.TRUE.equals(emEquipe)) {
            throw new IllegalArgumentException("O aluno " + nome
                    + " faz parte de uma equipe. Retire-o da equipe antes de trocar a turma.");
        }
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
