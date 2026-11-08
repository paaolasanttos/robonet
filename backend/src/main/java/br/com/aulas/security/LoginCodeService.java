package br.com.aulas.security;

import br.com.aulas.model.LogCodigoVerificacao;
import br.com.aulas.repository.LogCodigoVerificacaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginCodeService {
    private static final int MAX_ATTEMPTS = 5;
    private final SecureRandom random = new SecureRandom();
    private final LogCodigoVerificacaoRepository logCodigoVerificacaoRepository;
    private final Duration expiration;
    private final Duration cooldown;

    public LoginCodeService(
            LogCodigoVerificacaoRepository logCodigoVerificacaoRepository,
            @Value("${security.jwt.secret}") String ignoredHashSecret,
            @Value("${gmail.code-expiration-seconds}") long expirationSeconds,
            @Value("${gmail.resend-cooldown-seconds}") long cooldownSeconds) {
        this.logCodigoVerificacaoRepository = logCodigoVerificacaoRepository;
        this.expiration = Duration.ofSeconds(expirationSeconds);
        this.cooldown = Duration.ofSeconds(cooldownSeconds);
    }

    public String issue(String email) {
        return issue(email, "login");
    }

    public String issue(String email, String purpose) {
        String normalizedEmail = normalizeEmail(email);
        Optional<LogCodigoVerificacao> latest = logCodigoVerificacaoRepository.findTopByEmailIgnoreCaseAndTipoOrderByDtGeracaoDesc(normalizedEmail, purpose);

        if (latest.isPresent() && Duration.between(latest.get().getDtGeracao(), LocalDateTime.now()).compareTo(cooldown) < 0) {
            throw new IllegalStateException("Aguarde alguns segundos antes de solicitar outro código.");
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        LogCodigoVerificacao codigo = new LogCodigoVerificacao();
        codigo.setEmail(normalizedEmail);
        codigo.setCodigo(code);
        codigo.setTipo(purpose);
        codigo.setDtGeracao(LocalDateTime.now());
        logCodigoVerificacaoRepository.save(codigo);
        return code;
    }

    public boolean verify(String email, String code) {
        return verify(email, code, "login");
    }

    public boolean verify(String email, String code, String purpose) {
        String normalizedEmail = normalizeEmail(email);
        Optional<LogCodigoVerificacao> latest = logCodigoVerificacaoRepository.findTopByEmailIgnoreCaseAndTipoOrderByDtGeracaoDesc(normalizedEmail, purpose);

        if (latest.isEmpty()) {
            return false;
        }

        LogCodigoVerificacao codigoVerificacao = latest.get();
        LocalDateTime now = LocalDateTime.now();
        if (Duration.between(codigoVerificacao.getDtGeracao(), now).compareTo(expiration) > 0) {
            logCodigoVerificacaoRepository.deleteByEmailIgnoreCaseAndTipo(normalizedEmail, purpose);
            return false;
        }

        String codigoInformado = code == null ? "" : code.trim();
        if (codigoVerificacao.getCodigo().equals(codigoInformado)) {
            logCodigoVerificacaoRepository.deleteByEmailIgnoreCaseAndTipo(normalizedEmail, purpose);
            return true;
        }

        return false;
    }

    public long expirationSeconds() { return expiration.toSeconds(); }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
