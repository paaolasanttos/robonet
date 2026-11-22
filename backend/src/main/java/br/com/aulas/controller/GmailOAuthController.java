package br.com.aulas.controller;

import br.com.aulas.service.GmailOAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@RestController
@RequestMapping("/auth/gmail/oauth")
public class GmailOAuthController {
    private final GmailOAuthService gmailOAuthService;

    public GmailOAuthController(GmailOAuthService gmailOAuthService) {
        this.gmailOAuthService = gmailOAuthService;
    }

    @GetMapping("/authorize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> authorize() {
        try {
            return ResponseEntity.ok(Map.of("authorizationUrl", gmailOAuthService.createAuthorizationUrl()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(503).body(Map.of("message", exception.getMessage()));
        } catch (IOException | GeneralSecurityException exception) {
            return ResponseEntity.status(502).body(Map.of("message", "Não foi possível iniciar a autorização do Gmail."));
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error) {
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "A autorização do Gmail foi cancelada."));
        }
        try {
            gmailOAuthService.completeAuthorization(code, state);
            return ResponseEntity.ok(Map.of("message", "Gmail autorizado com sucesso."));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(503).body(Map.of("message", exception.getMessage()));
        } catch (IOException | GeneralSecurityException exception) {
            return ResponseEntity.status(502).body(Map.of("message", "Não foi possível concluir a autorização do Gmail."));
        }
    }
}
