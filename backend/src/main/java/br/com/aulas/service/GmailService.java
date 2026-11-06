package br.com.aulas.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
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
import jakarta.mail.MessagingException;

@Service
public class GmailService {
    private static final Logger logger = LoggerFactory.getLogger(GmailService.class);

    private final boolean enabled;
    private final String sender;
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;

    public GmailService(
            @Value("${gmail.enabled}") boolean enabled,
            @Value("${gmail.sender}") String sender,
            @Value("${gmail.client-id}") String clientId,
            @Value("${gmail.client-secret}") String clientSecret,
            @Value("${gmail.refresh-token}") String refreshToken) {
        this.enabled = enabled;
        this.sender = sender;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
    }

    public void sendLoginCode(String recipient, String code, long expirationSeconds) {
        if (!enabled) {
            throw new IllegalStateException("O envio de códigos por Gmail está desativado.");
        }
        if (sender.isBlank() || clientId.isBlank() || clientSecret.isBlank() || refreshToken.isBlank()) {
            throw new IllegalStateException("Configure GMAIL_SENDER, GMAIL_CLIENT_ID, GMAIL_CLIENT_SECRET e GMAIL_REFRESH_TOKEN.");
        }
        try {
            var transport = GoogleNetHttpTransport.newTrustedTransport();
            var credential = new GoogleCredential.Builder()
                    .setTransport(transport)
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .setClientSecrets(clientId, clientSecret)
                    .build()
                    .createScoped(java.util.List.of("https://www.googleapis.com/auth/gmail.send"))
                    .setRefreshToken(refreshToken);
            credential.refreshToken();

            Gmail gmail = new Gmail.Builder(transport, GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName("ROBONET")
                    .build();
            MimeMessage email = new MimeMessage(Session.getInstance(new Properties()));
            email.setFrom(new InternetAddress(sender));
            email.setRecipients(jakarta.mail.Message.RecipientType.TO, InternetAddress.parse(recipient));
            email.setSubject("Código de acesso ROBONET", StandardCharsets.UTF_8.name());
            email.setText("Seu código de acesso é: " + code + "\n\nEle expira em " + (expirationSeconds / 60) + " minutos.", StandardCharsets.UTF_8.name());

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            email.writeTo(output);
            Message message = new Message().setRaw(Base64.getUrlEncoder().withoutPadding().encodeToString(output.toByteArray()));
            gmail.users().messages().send("me", message).execute();
        } catch (IOException | GeneralSecurityException | MessagingException exception) {
            logger.error("Falha ao enviar código pelo Gmail ({}).", exception.getClass().getName(), exception);
            throw new IllegalStateException("Não foi possível enviar o código pelo Gmail.", exception);
        }
    }
}