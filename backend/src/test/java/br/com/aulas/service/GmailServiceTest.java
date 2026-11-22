package br.com.aulas.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class GmailServiceTest {

    @Test
    void rejectsOAuthScopeUsedAsSender() {
        GmailOAuthService oauthService = mock(GmailOAuthService.class);
        GmailEmailService emailService = mock(GmailEmailService.class);
        GmailService service = new GmailService(
                true,
                "https://www.googleapis.com/auth/gmail.send",
                oauthService,
                emailService);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.sendPasswordResetCode("user@example.com", "123456", 600));

        assertEquals("GMAIL_SENDER deve ser um endereço de e-mail válido.", exception.getMessage());
        assertFalse(service.isEnabled());
        verify(emailService, never()).sendPasswordResetCode(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong());
    }
}
