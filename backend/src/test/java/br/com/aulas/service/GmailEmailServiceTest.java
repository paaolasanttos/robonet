package br.com.aulas.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GmailEmailServiceTest {

    @Test
    void sendsPasswordResetCodeWithPurposeSpecificContent() throws Exception {
        GmailOAuthService oauthService = mock(GmailOAuthService.class);
        Gmail gmail = mock(Gmail.class);
        Gmail.Users users = mock(Gmail.Users.class);
        Gmail.Users.Messages messages = mock(Gmail.Users.Messages.class);
        Gmail.Users.Messages.Send send = mock(Gmail.Users.Messages.Send.class);
        when(oauthService.createGmailClient()).thenReturn(gmail);
        when(gmail.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.send(eq("me"), isA(Message.class))).thenReturn(send);
        when(send.execute()).thenReturn(new Message());

        GmailEmailService emailService = new GmailEmailService(oauthService, "no-reply@robonet.com");
        emailService.sendPasswordResetCode("user@example.com", "123456", 600);

        var messageCaptor = org.mockito.ArgumentCaptor.forClass(Message.class);
        verify(messages).send(eq("me"), messageCaptor.capture());
        MimeMessage email = new MimeMessage(Session.getInstance(new Properties()),
                new ByteArrayInputStream(Base64.getUrlDecoder().decode(messageCaptor.getValue().getRaw())));
        assertEquals("Recuperação de senha ROBONET", email.getSubject());
        assertTrue(email.getContent().toString().contains("Seu código para redefinir a senha é: 123456"));
        assertTrue(email.getContent().toString().contains("10 minutos"));
    }

    @Test
    void reportsGmailApiFailure() throws Exception {
        GmailOAuthService oauthService = mock(GmailOAuthService.class);
        Gmail gmail = mock(Gmail.class);
        Gmail.Users users = mock(Gmail.Users.class);
        Gmail.Users.Messages messages = mock(Gmail.Users.Messages.class);
        Gmail.Users.Messages.Send send = mock(Gmail.Users.Messages.Send.class);
        when(oauthService.createGmailClient()).thenReturn(gmail);
        when(gmail.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.send(eq("me"), isA(Message.class))).thenReturn(send);
        when(send.execute()).thenThrow(new IOException("Gmail unavailable"));

        GmailEmailService emailService = new GmailEmailService(oauthService, "no-reply@robonet.com");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> emailService.sendLoginCode("user@example.com", "123456", 600));

        assertEquals("Não foi possível enviar o código pelo Gmail.", exception.getMessage());
    }
}
