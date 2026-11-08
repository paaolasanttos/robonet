package br.com.aulas.service;

import br.com.aulas.dto.MeusDadosExportResponse;
import br.com.aulas.dto.MeusDadosResponse;
import br.com.aulas.dto.SolicitacaoAlteracaoResponse;
import br.com.aulas.dto.SenhaRequest;
import br.com.aulas.dto.SolicitacaoAlteracaoDadosRequest;
import br.com.aulas.dto.TurmaResponse;
import br.com.aulas.model.SolicitacaoExclusao;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.SolicitacaoExclusaoRepository;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.DatabaseUserContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class MeusDadosService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TurmaService turmaService;
    private final AuditoriaService auditoriaService;
    private final DatabaseUserContext databaseUserContext;
    private final SolicitacaoExclusaoRepository solicitacaoExclusaoRepository;
    private final JdbcTemplate jdbcTemplate;

    public MeusDadosService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                            TurmaService turmaService, AuditoriaService auditoriaService,
                            DatabaseUserContext databaseUserContext,
                            SolicitacaoExclusaoRepository solicitacaoExclusaoRepository, JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.turmaService = turmaService;
        this.auditoriaService = auditoriaService;
        this.databaseUserContext = databaseUserContext;
        this.solicitacaoExclusaoRepository = solicitacaoExclusaoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public MeusDadosResponse consultar(Usuario usuario) {
        SolicitacaoExclusao exclusao = solicitacaoExclusaoRepository
                .findByIdUsuarioAndStatus(usuario.getId(), ExclusaoContaService.PENDENTE).orElse(null);

        return new MeusDadosResponse(
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getRgm(),
                turmasDoUsuario(usuario),
                exclusao == null ? null : exclusao.getDtSolicitacao().toLocalDate(),
                exclusao == null ? null : exclusao.getDtLimite().toLocalDate(),
                turmaService.listar(),
                solicitacoesAlteracao(usuario.getEmail())
        );
    }

    private List<SolicitacaoAlteracaoResponse> solicitacoesAlteracao(String email) {
        return jdbcTemplate.query(
                "SELECT idusuario, nome, rgm, perfil, turma, status, motivo, dtregistro "
                        + "FROM usuariossolicitacoes WHERE LOWER(email) = LOWER(?) "
                        + "ORDER BY dtregistro DESC, idusuario DESC",
                (result, rowNumber) -> new SolicitacaoAlteracaoResponse(
                        result.getLong("idusuario"),
                        result.getString("nome"),
                        result.getString("rgm"),
                        result.getString("perfil"),
                        result.getString("turma"),
                        normalizarStatusSolicitacao(result.getString("status")),
                        result.getString("motivo"),
                        result.getTimestamp("dtregistro") == null
                                ? null : result.getTimestamp("dtregistro").toLocalDateTime()
                ),
                email
        );
    }

    private String normalizarStatusSolicitacao(String status) {
        if (status == null || status.isBlank()) return "Pendente de análise";
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "PENDENTE", "PENDENTE DE ANALISE", "PENDENTE DE ANÁLISE" -> "Pendente de análise";
            case "DEFERIDO" -> "Deferido";
            case "INDEFERIDO" -> "Indeferido";
            default -> status;
        };
    }

    public MeusDadosExportResponse exportar(Usuario usuario) {
        List<String> equipe = jdbcTemplate.queryForList(
                "SELECT e.nome FROM equipes e JOIN equipesmembros m ON m.idequipe = e.idequipe WHERE m.idusuario = ?",
                String.class, usuario.getId());
        List<String> aulas = jdbcTemplate.queryForList(
                "SELECT a.titulo FROM aulasaluno aa JOIN aulas a ON a.idaula = aa.idaula "
                        + "WHERE aa.idusuario = ? AND UPPER(aa.concluida) = 'S' ORDER BY a.titulo",
                String.class, usuario.getId());
        List<MeusDadosExportResponse.SimuladoRealizado> simulados = jdbcTemplate.query(
                "SELECT s.titulo, r.dtrealizacao FROM simuladosrealizacoes r LEFT JOIN simulados s ON s.idsimulado = r.idsimulado "
                        + "WHERE r.idusuario = ? ORDER BY r.dtrealizacao",
                (result, rowNumber) -> new MeusDadosExportResponse.SimuladoRealizado(
                        result.getString("titulo"), result.getTimestamp("dtrealizacao").toLocalDateTime()),
                usuario.getId());
        List<MeusDadosExportResponse.AceiteTermo> aceites = jdbcTemplate.query(
                "SELECT versao, ip, dtaceite FROM termosaceites WHERE idusuario = ? ORDER BY dtaceite",
                (result, rowNumber) -> new MeusDadosExportResponse.AceiteTermo(
                        result.getString("versao"), result.getString("ip"), result.getTimestamp("dtaceite").toLocalDateTime()),
                usuario.getId());
        List<LocalDateTime> acessos = jdbcTemplate.query(
                "SELECT dtacesso FROM logacessos WHERE LOWER(email) = LOWER(?) ORDER BY dtacesso",
                (result, rowNumber) -> result.getTimestamp("dtacesso").toLocalDateTime(),
                usuario.getEmail());

        auditoriaService.registrar("LGPD", "DADOS_EXPORTADOS",
                "Usuário " + usuario.getId() + " baixou uma cópia dos próprios dados", true);
        return new MeusDadosExportResponse(LocalDateTime.now(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(),
                usuario.getRgm(), turmasDoUsuario(usuario), equipe.isEmpty() ? null : equipe.get(0), aulas, simulados, aceites, acessos);
    }

    @Transactional
    public void solicitarAlteracao(Usuario usuario, SolicitacaoAlteracaoDadosRequest dados) {
        String perfil = dados.perfil().trim().toLowerCase(Locale.ROOT);
        if (!perfil.equals("aluno") && !perfil.equals("professor")) {
            throw new IllegalArgumentException("O perfil deve ser aluno ou professor.");
        }

        jdbcTemplate.update(
                "INSERT INTO usuariossolicitacoes (nome, email, rgm, perfil, turma, dtregistro, usrregistro) "
                        + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)",
                dados.nome().trim(),
                usuario.getEmail(),
                dados.rgm() == null || dados.rgm().isBlank() ? null : dados.rgm().trim(),
                perfil,
                dados.turma() == null || dados.turma().isBlank() ? null : dados.turma().trim(),
                usuario.getEmail()
        );
        auditoriaService.registrar("USUARIOS", "ALTERACAO_DADOS_SOLICITADA",
                "Usuário " + usuario.getId() + " solicitou alteração dos dados cadastrais", true);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void alterarSenha(Usuario usuario, SenhaRequest dados) {
        conferirSenha(usuario, dados.senhaAtual(), "SENHA_ALTERACAO_FALHA");
        if (passwordEncoder.matches(dados.novaSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual.");
        }

        databaseUserContext.setCurrentUser();
        usuario.setSenha(passwordEncoder.encode(dados.novaSenha()));
        usuarioRepository.save(usuario);
        auditoriaService.registrar("CRIPTOGRAFIA", "SENHA_ALTERADA",
                "Usuário " + usuario.getId() + " alterou a própria senha, salva com hash BCrypt", true);
    }

    private List<String> turmasDoUsuario(Usuario usuario) {
        return turmaService.listarDoUsuario(usuario.getEmail()).stream().map(TurmaResponse::nome).toList();
    }

    private void conferirSenha(Usuario usuario, String senha, String acaoFalha) {
        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            auditoriaService.registrar("USUARIOS", acaoFalha,
                    "Usuário " + usuario.getId() + " informou a senha atual incorreta", false);
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }
    }

}
