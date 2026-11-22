package br.com.aulas.service;

import br.com.aulas.dto.SolicitacaoAlteracaoAnaliseResponse;
import br.com.aulas.dto.TurmaResponse;
import br.com.aulas.exception.RegistroNaoEncontradoException;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.EquipeMembroRepository;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.DadoSensivel;
import br.com.aulas.security.DatabaseUserContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class SolicitacaoAlteracaoService {
    private static final String PENDENTE = "Pendente de análise";
    private static final String DEFERIDO = "Deferido";
    private static final String INDEFERIDO = "Indeferido";
    private static final String STATUS_PENDENTE_SQL =
            "UPPER(TRIM(status)) IN ('PENDENTE', 'PENDENTE DE ANALISE', 'PENDENTE DE ANÁLISE')";
    private static final String STATUS_PENDENTE_SELECT_SQL =
            "UPPER(TRIM(s.status)) IN ('PENDENTE', 'PENDENTE DE ANALISE', 'PENDENTE DE ANÁLISE')";

    private final JdbcTemplate jdbcTemplate;
    private final UsuarioRepository usuarioRepository;
    private final TurmaService turmaService;
    private final EquipeMembroRepository equipeMembroRepository;
    private final DatabaseUserContext databaseUserContext;
    private final AuditoriaService auditoriaService;

    public SolicitacaoAlteracaoService(JdbcTemplate jdbcTemplate, UsuarioRepository usuarioRepository,
                                       TurmaService turmaService, EquipeMembroRepository equipeMembroRepository,
                                       DatabaseUserContext databaseUserContext, AuditoriaService auditoriaService) {
        this.jdbcTemplate = jdbcTemplate;
        this.usuarioRepository = usuarioRepository;
        this.turmaService = turmaService;
        this.equipeMembroRepository = equipeMembroRepository;
        this.databaseUserContext = databaseUserContext;
        this.auditoriaService = auditoriaService;
    }

    private static final String SELECT_SQL =
            "SELECT s.idsolicitacaoalteracao AS idusuario, s.email, s.nome, s.ra, s.perfil, s.turma, s.status, s.motivo, "
                    + "s.dtregistro, s.dtanalise, u.nome AS nomeatual, u.ra AS raatual, "
                    + "u.perfil AS perfilatual, COALESCE((SELECT string_agg(t.nome, ', ' ORDER BY t.nome) "
                    + "FROM usuariosturmas ut JOIN turmas t ON t.idturma = ut.idturma "
                    + "WHERE ut.idusuario = u.idusuario), '-') AS turmaatual "
                    + "FROM solicitacoesalteracoes s LEFT JOIN usuarios u "
                    + "ON LOWER(TRIM(u.email)) = LOWER(TRIM(s.email)) AND u.status = 'Ativo' ";

    @Transactional(readOnly = true)
    public List<SolicitacaoAlteracaoAnaliseResponse> listar() {
        return jdbcTemplate.query(
                SELECT_SQL
                        + "ORDER BY CASE WHEN " + STATUS_PENDENTE_SELECT_SQL + " THEN 0 ELSE 1 END, "
                        + "s.dtregistro DESC, s.idsolicitacaoalteracao DESC",
                (result, rowNumber) -> resumo(result)
        );
    }

    @Transactional
    public SolicitacaoAlteracaoAnaliseResponse detalhar(Long id) {
        List<SolicitacaoAlteracaoAnaliseResponse> encontradas = jdbcTemplate.query(
                SELECT_SQL + "WHERE s.idsolicitacaoalteracao = ?",
                (result, rowNumber) -> response(result),
                id
        );
        if (encontradas.isEmpty()) {
            throw new RegistroNaoEncontradoException("Solicitação não encontrada.");
        }
        auditoriaService.registrar("LGPD", "DADO_SENSIVEL_VISUALIZADO",
                "Dados da solicitação de alteração " + id + " visualizados para análise", true);
        return encontradas.get(0);
    }

    private SolicitacaoAlteracaoAnaliseResponse resumo(ResultSet result) throws SQLException {
        return new SolicitacaoAlteracaoAnaliseResponse(
                result.getLong("idusuario"),
                DadoSensivel.mascararEmail(result.getString("email")),
                null, null, null, null,
                normalizarStatus(result.getString("status")),
                result.getString("motivo"),
                data(result, "dtregistro"),
                data(result, "dtanalise"),
                null, null, null, null
        );
    }

    @Transactional
    public void deferir(Long id, String emailAnalista) {
        Solicitacao solicitacao = buscarPendenteParaAtualizacao(id);
        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(solicitacao.email())
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "Não foi encontrado um usuário ativo para esta solicitação."));

        aplicarAlteracoes(solicitacao, usuario);
        databaseUserContext.setCurrentUser();
        int atualizados = jdbcTemplate.update(
                "UPDATE solicitacoesalteracoes SET status = ?, motivo = NULL, dtanalise = CURRENT_TIMESTAMP, "
                        + "usranalise = ? WHERE idsolicitacaoalteracao = ? AND " + STATUS_PENDENTE_SQL,
                DEFERIDO, emailAnalista, id
        );
        if (atualizados == 0) {
            throw new IllegalArgumentException("Esta solicitação já foi analisada.");
        }
        auditoriaService.registrar("USUARIOS", "ALTERACAO_DADOS_DEFERIDA",
                "Solicitação " + id + " deferida; cadastro do usuário " + usuario.getId() + " atualizado", true);
    }

    @Transactional
    public void indeferir(Long id, String emailAnalista, String motivo) {
        String motivoValidado = motivo == null ? "" : motivo.trim();
        if (motivoValidado.isEmpty()) {
            throw new IllegalArgumentException("Informe o motivo do indeferimento.");
        }
        if (motivoValidado.length() > 2000) {
            throw new IllegalArgumentException("O motivo deve ter até 2000 caracteres.");
        }

        Solicitacao solicitacao = buscarPendenteParaAtualizacao(id);
        int atualizados = jdbcTemplate.update(
                "UPDATE solicitacoesalteracoes SET status = ?, motivo = ?, dtanalise = CURRENT_TIMESTAMP, "
                        + "usranalise = ? WHERE idsolicitacaoalteracao = ? AND " + STATUS_PENDENTE_SQL,
                INDEFERIDO, motivoValidado, emailAnalista, id
        );
        if (atualizados == 0) {
            throw new IllegalArgumentException("Esta solicitação já foi analisada.");
        }
        auditoriaService.registrar("USUARIOS", "ALTERACAO_DADOS_INDEFERIDA",
                "Solicitação " + id + " do usuário " + solicitacao.email() + " indeferida", true);
    }

    private void aplicarAlteracoes(Solicitacao solicitacao, Usuario usuario) {
        String perfil = solicitacao.perfil().trim().toLowerCase(Locale.ROOT);
        if (!perfil.equals("aluno") && !perfil.equals("professor")) {
            throw new IllegalArgumentException("O perfil solicitado deve ser aluno ou professor.");
        }

        String nome = solicitacao.nome().trim();
        String ra = solicitacao.ra() == null || solicitacao.ra().isBlank()
                ? null : solicitacao.ra().trim();
        String turma = solicitacao.turma() == null || solicitacao.turma().isBlank()
                ? null : solicitacao.turma().trim();
        Long idTurma = turma == null ? null : localizarTurma(turma);
        boolean perfilAlterado = !perfil.equalsIgnoreCase(usuario.getPerfil());
        boolean nomeAlterado = !Objects.equals(nome, usuario.getNome());
        boolean turmaAlterada = turmaAlterada(usuario, turma);
        String raFinal = perfil.equals("aluno") ? ra : null;
        boolean raAlterado = !Objects.equals(raFinal, usuario.getRa());

        if (perfil.equals("aluno") && (ra == null || !ra.matches("\\d{11}"))) {
            throw new IllegalArgumentException("O RA solicitado deve conter exatamente 11 números.");
        }
        if (perfil.equals("aluno") && usuarioRepository.existsByRaAndIdNot(ra, usuario.getId())) {
            throw new IllegalArgumentException("Já existe um aluno com esse RA.");
        }
        if ((perfilAlterado || turmaAlterada) && equipeMembroRepository.existsByIdUsuario(usuario.getId())) {
            throw new IllegalArgumentException(
                    "O usuário faz parte de uma equipe. Retire-o da equipe antes de alterar o perfil ou a turma.");
        }

        if (nomeAlterado) usuario.setNome(nome);
        if (perfilAlterado) usuario.setPerfil(perfil);
        if (raAlterado) usuario.setRa(raFinal);
        if (nomeAlterado || perfilAlterado || raAlterado) {
            usuarioRepository.save(usuario);
        }
        if (turmaAlterada) {
            turmaService.salvarTurmasUsuario(usuario.getId(), idTurma == null ? null : List.of(idTurma));
        }
    }

    private boolean turmaAlterada(Usuario usuario, String turmaSolicitada) {
        List<TurmaResponse> turmasAtuais = turmaService.listarDoUsuario(usuario.getEmail());
        String turmaAtualPrincipal = turmasAtuais.isEmpty() ? null : turmasAtuais.get(0).nome();
        return !Objects.equals(turmaAtualPrincipal, turmaSolicitada);
    }

    private Long localizarTurma(String nome) {
        return turmaService.listar().stream()
                .filter(turma -> turma.nome().equalsIgnoreCase(nome))
                .map(TurmaResponse::id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("A turma solicitada não foi encontrada."));
    }

    private Solicitacao buscarPendenteParaAtualizacao(Long id) {
        List<Solicitacao> solicitacoes = jdbcTemplate.query(
                "SELECT idsolicitacaoalteracao AS idusuario, email, nome, ra, perfil, turma, status "
                        + "FROM solicitacoesalteracoes WHERE idsolicitacaoalteracao = ? FOR UPDATE",
                (result, rowNumber) -> new Solicitacao(
                        result.getLong("idusuario"), result.getString("email"), result.getString("nome"),
                        result.getString("ra"), result.getString("perfil"), result.getString("turma"),
                        result.getString("status")),
                id
        );
        if (solicitacoes.isEmpty()) {
            throw new RegistroNaoEncontradoException("Solicitação não encontrada.");
        }
        Solicitacao solicitacao = solicitacoes.get(0);
        if (!normalizarStatus(solicitacao.status()).equals(PENDENTE)) {
            throw new IllegalArgumentException("Esta solicitação já foi analisada.");
        }
        return solicitacao;
    }

    private String normalizarStatus(String status) {
        if (status == null) return "";
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "PENDENTE", "PENDENTE DE ANALISE", "PENDENTE DE ANÁLISE" -> PENDENTE;
            default -> status.trim();
        };
    }

    private SolicitacaoAlteracaoAnaliseResponse response(ResultSet result) throws SQLException {
        return new SolicitacaoAlteracaoAnaliseResponse(
                result.getLong("idusuario"),
                result.getString("email"),
                result.getString("nome"),
                result.getString("ra"),
                result.getString("perfil"),
                result.getString("turma"),
                normalizarStatus(result.getString("status")),
                result.getString("motivo"),
                data(result, "dtregistro"),
                data(result, "dtanalise"),
                result.getString("nomeatual"),
                result.getString("raatual"),
                result.getString("perfilatual"),
                result.getString("turmaatual")
        );
    }

    private LocalDateTime data(ResultSet result, String coluna) throws SQLException {
        return result.getTimestamp(coluna) == null ? null : result.getTimestamp(coluna).toLocalDateTime();
    }

    private record Solicitacao(Long id, String email, String nome, String ra, String perfil, String turma,
                               String status) { }
}
