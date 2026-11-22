package br.com.aulas.service;

import br.com.aulas.model.GmailToken;
import br.com.aulas.repository.GmailTokenRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GmailOAuthService {
    private static final long TOKEN_ID = 1L;
    private static final String GMAIL_SEND_SCOPE = "https://www.googleapis.com/auth/gmail.send";
    private static final Duration STATE_LIFETIME = Duration.ofMinutes(10);
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final GmailTokenRepository tokenRepository;
    private final String clientId;
    private final String clientSecret;
    private final String configuredRefreshToken;
    private final String redirectUri;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentMap<String, Instant> pendingStates = new ConcurrentHashMap<>();

    public GmailOAuthService(
            GmailTokenRepository tokenRepository,
            @Value("${gmail.client-id}") String clientId,
            @Value("${gmail.client-secret}") String clientSecret,
            @Value("${gmail.refresh-token}") String configuredRefreshToken,
            @Value("${gmail.redirect-uri:http://localhost:4000/auth/gmail/oauth/callback}") String redirectUri) {
        this.tokenRepository = tokenRepository;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.configuredRefreshToken = configuredRefreshToken;
        this.redirectUri = redirectUri;
    }

    public String createAuthorizationUrl() throws IOException, GeneralSecurityException {
        ensureClientConfigured();
        String state = newState();
        String url = createFlow().newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setResponseTypes(List.of("code"))
                .setState(state)
                .set("prompt", "consent")
                .build();
        pendingStates.put(state, Instant.now().plus(STATE_LIFETIME));
        pendingStates.entrySet().removeIf(entry -> entry.getValue().isBefore(Instant.now()));
        return url;
    }

    public void completeAuthorization(String code, String state)
            throws IOException, GeneralSecurityException {
        ensureClientConfigured();
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("O código de autorização do Gmail não foi informado.");
        }

        Instant expiresAt = state == null ? null : pendingStates.remove(state);
        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Estado OAuth inválido ou expirado.");
        }

        var tokens = createFlow().newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();
        String refreshToken = tokens.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalStateException(
                    "O Google não retornou refresh token. Revogue o acesso anterior e autorize novamente.");
        }
        tokenRepository.save(new GmailToken(TOKEN_ID, refreshToken, LocalDateTime.now()));
    }

    public Gmail createGmailClient() throws IOException, GeneralSecurityException {
        ensureClientConfigured();
        String refreshToken = tokenRepository.findById(TOKEN_ID)
                .map(GmailToken::getRefreshToken)
                .filter(token -> !token.isBlank())
                .orElse(configuredRefreshToken);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalStateException("Autorize o acesso ao Gmail ou configure GMAIL_REFRESH_TOKEN.");
        }

        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(transport)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientId, clientSecret)
                .build()
                .createScoped(List.of(GMAIL_SEND_SCOPE))
                .setRefreshToken(refreshToken);
        credential.refreshToken();

        return new Gmail.Builder(transport, JSON_FACTORY, credential)
                .setApplicationName("ROBONET")
                .build();
    }

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank()
                && (configuredRefreshToken != null && !configuredRefreshToken.isBlank()
                || tokenRepository.existsById(TOKEN_ID));
    }

    protected GoogleAuthorizationCodeFlow createFlow() throws IOException, GeneralSecurityException {
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleClientSecrets secrets = new GoogleClientSecrets()
                .setInstalled(new GoogleClientSecrets.Details()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setRedirectUris(List.of(redirectUri)));
        return new GoogleAuthorizationCodeFlow.Builder(
                transport, JSON_FACTORY, secrets, List.of(GMAIL_SEND_SCOPE))
                .setAccessType("offline")
                .build();
    }

    private void ensureClientConfigured() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("Configure GMAIL_CLIENT_ID e GMAIL_CLIENT_SECRET.");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new IllegalStateException("Configure GMAIL_REDIRECT_URI.");
        }
    }

    private String newState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
