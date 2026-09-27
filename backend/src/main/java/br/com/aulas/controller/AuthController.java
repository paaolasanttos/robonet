package br.com.aulas.controller;

import br.com.aulas.dto.AuthResponse;
import br.com.aulas.dto.LoginRequest;
import br.com.aulas.dto.RequestCodeRequest;
import br.com.aulas.dto.VerifyCodeRequest;
import br.com.aulas.dto.PasswordResetRequest;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.JwtService;
import br.com.aulas.security.LoginCodeService;
import br.com.aulas.service.AuditoriaService;
import br.com.aulas.service.GmailService;
import br.com.aulas.service.TermoService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginCodeService loginCodeService;
    private final GmailService gmailService;
    private final JdbcTemplate jdbcTemplate;
    private final AuditoriaService auditoriaService;
    private final TermoService termoService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                          LoginCodeService loginCodeService, GmailService gmailService, JdbcTemplate jdbcTemplate,
                          AuditoriaService auditoriaService, TermoService termoService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginCodeService = loginCodeService;
        this.gmailService = gmailService;
        this.jdbcTemplate = jdbcTemplate;
        this.auditoriaService = auditoriaService;
        this.termoService = termoService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.status(410).body(new ErrorResponse("O login agora exige um código enviado por e-mail."));
    }

    @PostMapping("/request-code")
    public ResponseEntity<?> requestCode(@RequestBody RequestCodeRequest request) {
        if (request.getEmail() == null || request.getSenha() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Informe e-mail e senha."));
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(request.getEmail().trim()).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo()) || usuario.getSenha() == null
                || !passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            auditoriaService.registrar(request.getEmail().trim(), "AUTENTICACAO", "LOGIN_FALHA",
                    "E-mail ou senha inválidos na primeira etapa do login", false);
            return ResponseEntity.status(401).body(new ErrorResponse("E-mail ou senha inválidos."));
        }

        try {
            String code = loginCodeService.issue(usuario.getEmail());
            gmailService.sendLoginCode(usuario.getEmail(), code, loginCodeService.expirationSeconds());
            auditoriaService.registrar(usuario.getEmail(), "AUTENTICACAO", "CODIGO_LOGIN_ENVIADO",
                    "Senha conferida com hash BCrypt e código de acesso enviado por e-mail", true);
            return ResponseEntity.ok(new CodeSentResponse("Código enviado para o e-mail cadastrado."));
        } catch (IllegalStateException exception) {
            auditoriaService.registrar(usuario.getEmail(), "AUTENTICACAO", "CODIGO_LOGIN_ERRO", exception.getMessage(), false);
            return ResponseEntity.status(503).body(new ErrorResponse(exception.getMessage()));
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest request) {
        if (request.getEmail() == null || request.getCodigo() == null || request.getCodigo().length() != 6) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Informe o e-mail e o código de 6 dígitos."));
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(request.getEmail().trim()).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo()) || !loginCodeService.verify(usuario.getEmail(), request.getCodigo())) {
            auditoriaService.registrar(request.getEmail().trim(), "AUTENTICACAO", "CODIGO_INVALIDO",
                    "Código de acesso inválido ou expirado", false);
            return ResponseEntity.status(401).body(new ErrorResponse("Código inválido ou expirado."));
        }

        jdbcTemplate.update("INSERT INTO logacessos (email) VALUES (?)", usuario.getEmail());
        boolean termoAceito = termoService.aceitouVersaoAtual(usuario);
        String token = jwtService.generate(usuario.getEmail(), usuario.getPerfil(), termoAceito);
        auditoriaService.registrar(usuario.getEmail(), "AUTENTICACAO", "LOGIN",
                "Login concluído e token JWT assinado emitido para o perfil " + usuario.getPerfil(), true);
        var resumo = new AuthResponse.UsuarioResumo(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(), termoAceito);
        return ResponseEntity.ok(new AuthResponse(token, resumo));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            auditoriaService.registrar(authentication.getName(), "AUTENTICACAO", "LOGOUT", "Usuário saiu da plataforma", true);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-code")
    public ResponseEntity<?> requestPasswordCode(@RequestBody RequestCodeRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Informe o e-mail."));
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(request.getEmail().trim()).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())) {
            return ResponseEntity.ok(new CodeSentResponse("Se o e-mail estiver cadastrado, um código será enviado."));
        }

        try {
            String code = loginCodeService.issue(usuario.getEmail(), "password");
            gmailService.sendLoginCode(usuario.getEmail(), code, loginCodeService.expirationSeconds());
            auditoriaService.registrar(usuario.getEmail(), "AUTENTICACAO", "CODIGO_SENHA_ENVIADO",
                    "Código de recuperação de senha enviado por e-mail", true);
            return ResponseEntity.ok(new CodeSentResponse("Se o e-mail estiver cadastrado, um código será enviado."));
        } catch (IllegalStateException exception) {
            auditoriaService.registrar(usuario.getEmail(), "AUTENTICACAO", "CODIGO_SENHA_ERRO", exception.getMessage(), false);
            return ResponseEntity.status(503).body(new ErrorResponse(exception.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        if (request.getEmail() == null || request.getCodigo() == null || request.getCodigo().length() != 6
                || request.getSenha() == null || request.getSenha().length() < 8) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Informe e-mail, código de 6 dígitos e senha com pelo menos 8 caracteres."));
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(request.getEmail().trim()).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())
                || !loginCodeService.verify(usuario.getEmail(), request.getCodigo(), "password")) {
            auditoriaService.registrar(request.getEmail().trim(), "AUTENTICACAO", "REDEFINIR_SENHA_FALHA",
                    "Código de recuperação inválido ou expirado", false);
            return ResponseEntity.status(401).body(new ErrorResponse("Código inválido ou expirado."));
        }

        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuarioRepository.save(usuario);
        auditoriaService.registrar(usuario.getEmail(), "CRIPTOGRAFIA", "SENHA_REDEFINIDA",
                "Nova senha criptografada com hash BCrypt e salva", true);
        return ResponseEntity.ok(new CodeSentResponse("Senha definida com sucesso. Faça login para continuar."));
    }

    private record ErrorResponse(String message) {}
    private record CodeSentResponse(String message) {}
}
