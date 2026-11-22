package br.com.aulas.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Properties;

@Service
public class GmailEmailService {
    private static final Logger logger = LoggerFactory.getLogger(GmailEmailService.class);

    private final GmailOAuthService gmailOAuthService;
    private final String sender;

    public GmailEmailService(
            GmailOAuthService gmailOAuthService,
            @Value("${gmail.sender}") String sender) {
        this.gmailOAuthService = gmailOAuthService;
        this.sender = sender;
    }

    public void sendLoginCode(String recipient, String code, long expirationSeconds) {
        sendCode(recipient, code, expirationSeconds, "Código de acesso ROBONET",
                "Seu código de acesso é: ");
    }

    public void sendPasswordResetCode(String recipient, String code, long expirationSeconds) {
        sendCode(recipient, code, expirationSeconds, "Recuperação de senha ROBONET",
                "Seu código para redefinir a senha é: ");
    }

    private void sendCode(String recipient, String code, long expirationSeconds, String subject, String messagePrefix) {
        try {
            Gmail gmail = gmailOAuthService.createGmailClient();
            MimeMessage email = new MimeMessage(Session.getInstance(new Properties()));
            email.setFrom(new InternetAddress(sender));
            email.setRecipients(jakarta.mail.Message.RecipientType.TO, InternetAddress.parse(recipient));
            email.setSubject(subject, StandardCharsets.UTF_8.name());
            email.setText(messagePrefix + code + "\n\nEle expira em "
                    + (expirationSeconds / 60) + " minutos.", StandardCharsets.UTF_8.name());

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            email.writeTo(output);
            Message message = new Message().setRaw(
                    Base64.getUrlEncoder().withoutPadding().encodeToString(output.toByteArray()));
            gmail.users().messages().send("me", message).execute();
        } catch (IOException | GeneralSecurityException | jakarta.mail.MessagingException exception) {
            logger.error("Falha ao enviar código pelo Gmail ({}).", exception.getClass().getName(), exception);
            throw new IllegalStateException("Não foi possível enviar o código pelo Gmail.", exception);
        }
    }
}
