package br.com.aulas.service;

import br.com.aulas.dto.SolicitacaoAlteracaoAnaliseResponse;
import br.com.aulas.dto.TurmaResponse;
import br.com.aulas.exception.RegistroNaoEncontradoException;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.EquipeMembroRepository;
import br.com.aulas.repository.UsuarioRepository;
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

    @Transactional(readOnly = true)
    public List<SolicitacaoAlteracaoAnaliseResponse> listar() {
        return jdbcTemplate.query(
                "SELECT s.idusuario, s.email, s.nome, s.rgm, s.perfil, s.turma, s.status, s.motivo, "
                        + "s.dtregistro, s.dtanalise, u.nome AS nomeatual, u.rgm AS rgmatual, "
                        + "u.perfil AS perfilatual, COALESCE((SELECT string_agg(t.nome, ', ' ORDER BY t.nome) "
                        + "FROM usuariosturmas ut JOIN turmas t ON t.idturma = ut.idturma "
                        + "WHERE ut.idusuario = u.idusuario), '-') AS turmaatual "
                        + "FROM usuariossolicitacoes s LEFT JOIN usuarios u "
                        + "ON LOWER(TRIM(u.email)) = LOWER(TRIM(s.email)) AND u.status = 'Ativo' "
                        + "ORDER BY CASE WHEN " + STATUS_PENDENTE_SELECT_SQL + " THEN 0 ELSE 1 END, "
                        + "s.dtregistro DESC, s.idusuario DESC",
                (result, rowNumber) -> response(result)
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
                "UPDATE usuariossolicitacoes SET status = ?, motivo = NULL, dtanalise = CURRENT_TIMESTAMP, "
                        + "usranalise = ? WHERE idusuario = ? AND " + STATUS_PENDENTE_SQL,
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
                "UPDATE usuariossolicitacoes SET status = ?, motivo = ?, dtanalise = CURRENT_TIMESTAMP, "
                        + "usranalise = ? WHERE idusuario = ? AND " + STATUS_PENDENTE_SQL,
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
        String rgm = solicitacao.rgm() == null || solicitacao.rgm().isBlank()
                ? null : solicitacao.rgm().trim();
        String turma = solicitacao.turma() == null || solicitacao.turma().isBlank()
                ? null : solicitacao.turma().trim();
        Long idTurma = turma == null ? null : localizarTurma(turma);
        boolean perfilAlterado = !perfil.equalsIgnoreCase(usuario.getPerfil());
        boolean nomeAlterado = !Objects.equals(nome, usuario.getNome());
        boolean turmaAlterada = turmaAlterada(usuario, turma);
        String rgmFinal = perfil.equals("aluno") ? rgm : null;
        boolean rgmAlterado = !Objects.equals(rgmFinal, usuario.getRgm());

        if (perfil.equals("aluno") && (rgm == null || !rgm.matches("\\d{11}"))) {
            throw new IllegalArgumentException("O RGM solicitado deve conter exatamente 11 números.");
        }
        if (perfil.equals("aluno") && usuarioRepository.existsByRgmAndIdNot(rgm, usuario.getId())) {
            throw new IllegalArgumentException("Já existe um aluno com esse RGM.");
        }
        if ((perfilAlterado || turmaAlterada) && equipeMembroRepository.existsByIdUsuario(usuario.getId())) {
            throw new IllegalArgumentException(
                    "O usuário faz parte de uma equipe. Retire-o da equipe antes de alterar o perfil ou a turma.");
        }

        if (nomeAlterado) usuario.setNome(nome);
        if (perfilAlterado) usuario.setPerfil(perfil);
        if (rgmAlterado) usuario.setRgm(rgmFinal);
        if (nomeAlterado || perfilAlterado || rgmAlterado) {
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
                "SELECT idusuario, email, nome, rgm, perfil, turma, status FROM usuariossolicitacoes "
                        + "WHERE idusuario = ? FOR UPDATE",
                (result, rowNumber) -> new Solicitacao(
                        result.getLong("idusuario"), result.getString("email"), result.getString("nome"),
                        result.getString("rgm"), result.getString("perfil"), result.getString("turma"),
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
                result.getString("rgm"),
                result.getString("perfil"),
                result.getString("turma"),
                normalizarStatus(result.getString("status")),
                result.getString("motivo"),
                data(result, "dtregistro"),
                data(result, "dtanalise"),
                result.getString("nomeatual"),
                result.getString("rgmatual"),
                result.getString("perfilatual"),
                result.getString("turmaatual")
        );
    }

    private LocalDateTime data(ResultSet result, String coluna) throws SQLException {
        return result.getTimestamp(coluna) == null ? null : result.getTimestamp(coluna).toLocalDateTime();
    }

    private record Solicitacao(Long id, String email, String nome, String rgm, String perfil, String turma,
                               String status) { }
}
