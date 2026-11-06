package br.com.aulas.service;

import br.com.aulas.dto.AuthResponse;
import br.com.aulas.dto.EmailCodigoRequest;
import br.com.aulas.dto.EmailConfirmacaoRequest;
import br.com.aulas.dto.MeusDadosResponse;
import br.com.aulas.dto.SenhaRequest;
import br.com.aulas.dto.TurmaResponse;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.DatabaseUserContext;
import br.com.aulas.security.JwtService;
import br.com.aulas.security.LoginCodeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public MeusDadosService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                            LoginCodeService loginCodeService, GmailService gmailService, JwtService jwtService,
                            TermoService termoService, TurmaService turmaService, AuditoriaService auditoriaService,
                            DatabaseUserContext databaseUserContext) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginCodeService = loginCodeService;
        this.gmailService = gmailService;
        this.jwtService = jwtService;
        this.termoService = termoService;
        this.turmaService = turmaService;
        this.auditoriaService = auditoriaService;
        this.databaseUserContext = databaseUserContext;
    }

    public MeusDadosResponse consultar(Usuario usuario) {
        return new MeusDadosResponse(
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getRgm(),
                turmaService.listarDoUsuario(usuario.getEmail()).stream().map(TurmaResponse::nome).toList()
        );
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
