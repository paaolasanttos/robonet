package br.com.aulas.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GmailTokenGeneratorTest {

    @Test
    void refreshAccessTokenRequiresRefreshToken() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> GmailTokenGenerator.refreshAccessToken(null, "client-id", "client-secret", " ")
        );

        assertEquals("GMAIL_REFRESH_TOKEN não foi configurado.", exception.getMessage());
    }
}
