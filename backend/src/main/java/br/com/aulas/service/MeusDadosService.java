package br.com.aulas.service;

import br.com.aulas.dto.AuthResponse;
import br.com.aulas.dto.EmailCodigoRequest;
import br.com.aulas.dto.EmailConfirmacaoRequest;
import br.com.aulas.dto.MeusDadosExportResponse;
import br.com.aulas.dto.MeusDadosResponse;
import br.com.aulas.dto.SenhaRequest;
import br.com.aulas.dto.TurmaResponse;
import br.com.aulas.model.SolicitacaoExclusao;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.SolicitacaoExclusaoRepository;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.DatabaseUserContext;
import br.com.aulas.security.JwtService;
import br.com.aulas.security.LoginCodeService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MeusDadosService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginCodeService loginCodeService;
    private final GmailService gmailService;
    private final JwtService jwtService;
    private final TermoService termoService;
    private final TurmaService turmaService;
    private final AuditoriaService auditoriaService;
    private final DatabaseUserContext databaseUserContext;
    private final SolicitacaoExclusaoRepository solicitacaoExclusaoRepository;
    private final JdbcTemplate jdbcTemplate;

    public MeusDadosService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                            LoginCodeService loginCodeService, GmailService gmailService, JwtService jwtService,
                            TermoService termoService, TurmaService turmaService, AuditoriaService auditoriaService,
                            DatabaseUserContext databaseUserContext,
                            SolicitacaoExclusaoRepository solicitacaoExclusaoRepository, JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginCodeService = loginCodeService;
        this.gmailService = gmailService;
        this.jwtService = jwtService;
        this.termoService = termoService;
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
                exclusao == null ? null : exclusao.getDtLimite().toLocalDate()
        );
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

    public void solicitarCodigoEmail(Usuario usuario, EmailCodigoRequest dados) {
        conferirSenha(usuario, dados.senhaAtual(), "EMAIL_ALTERACAO_FALHA");
        String novoEmail = validarNovoEmail(usuario, dados.novoEmail());

        String codigo = loginCodeService.issue(novoEmail, tipoCodigoEmail(usuario));
        gmailService.sendLoginCode(novoEmail, codigo, loginCodeService.expirationSeconds());
        auditoriaService.registrar("USUARIOS", "EMAIL_CODIGO_ENVIADO",
                "Usuário " + usuario.getId() + " solicitou a troca de e-mail", true);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public AuthResponse confirmarEmail(Usuario usuario, EmailConfirmacaoRequest dados) {
        String novoEmail = validarNovoEmail(usuario, dados.novoEmail());
        if (!loginCodeService.verify(novoEmail, dados.codigo(), tipoCodigoEmail(usuario))) {
            auditoriaService.registrar("USUARIOS", "EMAIL_ALTERACAO_FALHA",
                    "Usuário " + usuario.getId() + " informou código inválido ou expirado", false);
            throw new IllegalArgumentException("Código inválido ou expirado.");
        }

        databaseUserContext.setCurrentUser();
        String emailAnterior = usuario.getEmail();
        usuario.setEmail(novoEmail);
        Usuario salvo = usuarioRepository.save(usuario);
        auditoriaService.registrar("USUARIOS", "EMAIL_ALTERADO",
                "Usuário " + salvo.getId() + " alterou o e-mail de " + emailAnterior + " para " + novoEmail, true);

        boolean termoAceito = termoService.aceitouVersaoAtual(salvo);
        String token = jwtService.generate(salvo.getEmail(), salvo.getPerfil(), termoAceito);
        return new AuthResponse(token, new AuthResponse.UsuarioResumo(salvo.getId(), salvo.getNome(), salvo.getEmail(),
                salvo.getPerfil(), termoAceito));
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

    private String validarNovoEmail(Usuario usuario, String novoEmail) {
        String email = novoEmail.trim();
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        if (email.equalsIgnoreCase(usuario.getEmail())) {
            throw new IllegalArgumentException("O novo e-mail deve ser diferente do atual.");
        }
        if (usuarioRepository.existsByEmailIgnoreCaseAndAtivoTrue(email)) {
            throw new IllegalArgumentException("Já existe um usuário ativo com esse e-mail.");
        }
        return email;
    }

    private String tipoCodigoEmail(Usuario usuario) {
        return "email-" + usuario.getId();
    }
}
