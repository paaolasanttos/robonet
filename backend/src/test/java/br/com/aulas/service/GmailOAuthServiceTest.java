package br.com.aulas.service;

import br.com.aulas.model.GmailToken;
import br.com.aulas.repository.GmailTokenRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GmailOAuthServiceTest {

    @Test
    void exchangesOneTimeAuthorizationStateAndStoresRefreshToken() throws Exception {
        GmailTokenRepository repository = mock(GmailTokenRepository.class);
        GoogleAuthorizationCodeFlow flow = mock(GoogleAuthorizationCodeFlow.class);
        GoogleAuthorizationCodeRequestUrl authorizationUrl = mock(GoogleAuthorizationCodeRequestUrl.class);
        GoogleAuthorizationCodeTokenRequest tokenRequest = mock(GoogleAuthorizationCodeTokenRequest.class);
        GoogleTokenResponse tokenResponse = mock(GoogleTokenResponse.class);
        TestableGmailOAuthService service = new TestableGmailOAuthService(repository, flow);

        when(flow.newAuthorizationUrl()).thenReturn(authorizationUrl);
        when(authorizationUrl.setRedirectUri(anyString())).thenReturn(authorizationUrl);
        when(authorizationUrl.setState(anyString())).thenReturn(authorizationUrl);
        when(authorizationUrl.set("prompt", "consent")).thenReturn(authorizationUrl);
        when(authorizationUrl.build()).thenReturn("https://accounts.google.com/authorize");
        when(flow.newTokenRequest("auth-code")).thenReturn(tokenRequest);
        when(tokenRequest.setRedirectUri(anyString())).thenReturn(tokenRequest);
        when(tokenRequest.execute()).thenReturn(tokenResponse);
        when(tokenResponse.getRefreshToken()).thenReturn("refresh-token");

        service.createAuthorizationUrl();
        org.mockito.ArgumentCaptor<String> stateCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(authorizationUrl).setState(stateCaptor.capture());
        service.completeAuthorization("auth-code", stateCaptor.getValue());

        org.mockito.ArgumentCaptor<GmailToken> tokenCaptor = org.mockito.ArgumentCaptor.forClass(GmailToken.class);
        verify(repository).save(tokenCaptor.capture());
        assertEquals(1L, tokenCaptor.getValue().getId());
        assertEquals("refresh-token", tokenCaptor.getValue().getRefreshToken());
        assertThrows(IllegalArgumentException.class,
                () -> service.completeAuthorization("auth-code", stateCaptor.getValue()));
    }

    @Test
    void rejectsUnknownStateBeforeExchangingAuthorizationCode() throws Exception {
        GmailTokenRepository repository = mock(GmailTokenRepository.class);
        GoogleAuthorizationCodeFlow flow = mock(GoogleAuthorizationCodeFlow.class);
        TestableGmailOAuthService service = new TestableGmailOAuthService(repository, flow);

        assertThrows(IllegalArgumentException.class,
                () -> service.completeAuthorization("auth-code", "unrecognized-state"));

        verify(flow, never()).newTokenRequest(anyString());
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(GmailToken.class));
    }

    private static class TestableGmailOAuthService extends GmailOAuthService {
        private final GoogleAuthorizationCodeFlow flow;

        TestableGmailOAuthService(GmailTokenRepository repository, GoogleAuthorizationCodeFlow flow) {
            super(repository, "client-id", "client-secret", "", "http://localhost/callback");
            this.flow = flow;
        }

        @Override
        protected GoogleAuthorizationCodeFlow createFlow() {
            return flow;
        }
    }
}
