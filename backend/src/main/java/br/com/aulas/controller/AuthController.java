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
import br.com.aulas.service.GmailService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
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

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                          LoginCodeService loginCodeService, GmailService gmailService, JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginCodeService = loginCodeService;
        this.gmailService = gmailService;
        this.jdbcTemplate = jdbcTemplate;
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
            return ResponseEntity.status(401).body(new ErrorResponse("E-mail ou senha inválidos."));
        }

        try {
            String code = loginCodeService.issue(usuario.getEmail());
            gmailService.sendLoginCode(usuario.getEmail(), code, loginCodeService.expirationSeconds());
            return ResponseEntity.ok(new CodeSentResponse("Código enviado para o e-mail cadastrado."));
        } catch (IllegalStateException exception) {
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
            return ResponseEntity.status(401).body(new ErrorResponse("Código inválido ou expirado."));
        }

        jdbcTemplate.update("INSERT INTO logacessos (email) VALUES (?)", usuario.getEmail());
        String token = jwtService.generate(usuario.getEmail(), usuario.getPerfil());
        var resumo = new AuthResponse.UsuarioResumo(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
        return ResponseEntity.ok(new AuthResponse(token, resumo));
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
            return ResponseEntity.ok(new CodeSentResponse("Se o e-mail estiver cadastrado, um código será enviado."));
        } catch (IllegalStateException exception) {
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
            return ResponseEntity.status(401).body(new ErrorResponse("Código inválido ou expirado."));
        }

        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(new CodeSentResponse("Senha definida com sucesso. Faça login para continuar."));
    }

    private record ErrorResponse(String message) {}
    private record CodeSentResponse(String message) {}
}
