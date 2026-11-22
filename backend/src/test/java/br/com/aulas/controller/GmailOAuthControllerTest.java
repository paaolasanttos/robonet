package br.com.aulas.controller;

import br.com.aulas.service.GmailOAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GmailOAuthControllerTest {

    @Test
    void returnsAuthorizationUrl() throws Exception {
        GmailOAuthService service = mock(GmailOAuthService.class);
        when(service.createAuthorizationUrl()).thenReturn("https://accounts.google.com/authorize");

        ResponseEntity<?> response = new GmailOAuthController(service).authorize();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("https://accounts.google.com/authorize",
                ((java.util.Map<?, ?>) response.getBody()).get("authorizationUrl"));
    }

    @Test
    void callbackStoresAuthorizationAndReturnsSuccess() throws Exception {
        GmailOAuthService service = mock(GmailOAuthService.class);
        GmailOAuthController controller = new GmailOAuthController(service);

        ResponseEntity<?> response = controller.callback("auth-code", "state", null);

        assertEquals(200, response.getStatusCode().value());
        verify(service).completeAuthorization("auth-code", "state");
    }
}
