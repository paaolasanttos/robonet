package br.com.aulas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class GmailService {
    private static final Logger logger = LoggerFactory.getLogger(GmailService.class);
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final boolean enabled;
    private final String sender;
    private final GmailOAuthService gmailOAuthService;
    private final GmailEmailService gmailEmailService;

    public GmailService(
            @Value("${gmail.enabled}") boolean enabled,
            @Value("${gmail.sender}") String sender,
            GmailOAuthService gmailOAuthService,
            GmailEmailService gmailEmailService) {
        this.enabled = enabled;
        this.sender = sender;
        this.gmailOAuthService = gmailOAuthService;
        this.gmailEmailService = gmailEmailService;
    }

    public boolean isEnabled() {
        return enabled && isValidSender() && gmailOAuthService.isConfigured();
    }

    public void sendLoginCode(String recipient, String code, long expirationSeconds) {
        if (!enabled) {
            throw new IllegalStateException("O envio de códigos por Gmail está desativado.");
        }
        if (!isValidSender()) {
            throw new IllegalStateException("GMAIL_SENDER deve ser um endereço de e-mail válido.");
        }
        if (!gmailOAuthService.isConfigured()) {
            throw new IllegalStateException("Configure GMAIL_CLIENT_ID, GMAIL_CLIENT_SECRET e autorize o Gmail.");
        }
        try {
            gmailEmailService.sendLoginCode(recipient, code, expirationSeconds);
        } catch (IllegalStateException exception) {
            logger.error("Falha ao enviar código de acesso pelo Gmail.", exception);
            throw new IllegalStateException("Não foi possível enviar o código pelo Gmail.", exception);
        }
    }

    public void sendPasswordResetCode(String recipient, String code, long expirationSeconds) {
        if (!enabled) {
            throw new IllegalStateException("O envio de códigos por Gmail está desativado.");
        }
        if (!isValidSender()) {
            throw new IllegalStateException("GMAIL_SENDER deve ser um endereço de e-mail válido.");
        }
        if (!gmailOAuthService.isConfigured()) {
            throw new IllegalStateException("Configure GMAIL_CLIENT_ID, GMAIL_CLIENT_SECRET e autorize o Gmail.");
        }
        try {
            gmailEmailService.sendPasswordResetCode(recipient, code, expirationSeconds);
        } catch (IllegalStateException exception) {
            logger.error("Falha ao enviar código de recuperação pelo Gmail.", exception);
            throw new IllegalStateException("Não foi possível enviar o código pelo Gmail.", exception);
        }
    }

    private boolean isValidSender() {
        return sender != null && EMAIL_PATTERN.matcher(sender.trim()).matches();
    }
}