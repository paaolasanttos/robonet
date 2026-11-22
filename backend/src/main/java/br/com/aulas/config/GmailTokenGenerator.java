package br.com.aulas.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Properties;

public final class GmailTokenGenerator {
    private static final String GMAIL_SEND_SCOPE = "https://www.googleapis.com/auth/gmail.send";
    private static final int CALLBACK_PORT = 8888;
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private GmailTokenGenerator() { }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        String clientId = setting("GMAIL_CLIENT_ID");
        String clientSecret = setting("GMAIL_CLIENT_SECRET");
        Tokens tokens = authorizeAndRefresh(clientId, clientSecret);

        System.out.println("GMAIL_REFRESH_TOKEN=" + tokens.refreshToken());
        System.out.println("Access token obtido usando o refresh token (não exibido).");
        System.out.println("Validade do access token: " + tokens.expiresInSeconds() + " segundos.");
    }

    public static Tokens authorizeAndRefresh(String clientId, String clientSecret)
            throws IOException, GeneralSecurityException {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalArgumentException("Configure GMAIL_CLIENT_ID e GMAIL_CLIENT_SECRET.");
        }

        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleClientSecrets secrets = new GoogleClientSecrets()
                .setInstalled(new GoogleClientSecrets.Details()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setRedirectUris(List.of("http://localhost:" + CALLBACK_PORT + "/Callback")));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport, JSON_FACTORY, secrets, List.of(GMAIL_SEND_SCOPE))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(CALLBACK_PORT).build();
        try {
            String redirectUri = receiver.getRedirectUri();
            String authorizationUrl = flow.newAuthorizationUrl()
                    .setRedirectUri(redirectUri)
                    .set("prompt", "consent")
                    .build();

            System.out.println("Abra esta URL e autorize o acesso ao Gmail:");
            System.out.println(authorizationUrl);
            openBrowser(authorizationUrl);

            String authorizationCode = receiver.waitForCode();
            GoogleTokenResponse authorizedTokens = flow.newTokenRequest(authorizationCode)
                    .setRedirectUri(redirectUri)
                    .execute();

            String refreshToken = authorizedTokens.getRefreshToken();
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new IllegalStateException(
                        "O Google não retornou refresh token. Revogue o acesso anterior da aplicação e autorize novamente.");
            }

            GoogleTokenResponse refreshedTokens = refreshAccessToken(
                    transport, clientId, clientSecret, refreshToken);
            return new Tokens(refreshToken, refreshedTokens.getAccessToken(), refreshedTokens.getExpiresInSeconds());
        } finally {
            receiver.stop();
        }
    }

    public static GoogleTokenResponse refreshAccessToken(
            NetHttpTransport transport, String clientId, String clientSecret, String refreshToken)
            throws IOException {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("GMAIL_REFRESH_TOKEN não foi configurado.");
        }
        return new GoogleRefreshTokenRequest(
                transport, JSON_FACTORY, refreshToken, clientId, clientSecret)
                .execute();
    }

    private static void openBrowser(String authorizationUrl) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            System.out.println("O navegador não pôde ser aberto automaticamente; use a URL acima.");
            return;
        }
        try {
            Desktop.getDesktop().browse(java.net.URI.create(authorizationUrl));
        } catch (IOException | UnsupportedOperationException exception) {
            System.out.println("O navegador não pôde ser aberto automaticamente; use a URL acima.");
        }
    }

    private static String setting(String name) throws IOException {
        String environmentValue = System.getenv(name);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        for (Path path : List.of(Path.of(".env"), Path.of("..", ".env"))) {
            if (!Files.isRegularFile(path)) {
                continue;
            }
            Properties properties = new Properties();
            try (var reader = Files.newBufferedReader(path)) {
                properties.load(reader);
            }
            String value = properties.getProperty(name);
            if (value != null && !value.isBlank()) {
                return stripQuotes(value.trim());
            }
        }
        throw new IllegalStateException(name + " não foi encontrado nas variáveis de ambiente nem no arquivo .env.");
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    public record Tokens(String refreshToken, String accessToken, Long expiresInSeconds) { }
}
