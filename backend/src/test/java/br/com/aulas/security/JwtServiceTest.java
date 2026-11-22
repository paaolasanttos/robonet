package br.com.aulas.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void geraTokenComDadosDeUsuarioERole() {
        JwtService service = new JwtService("12345678901234567890123456789012", 60_000);

        String token = service.generate("aluno@umc.br", "Aluno", true);
        Claims claims = service.parse(token);

        assertEquals("aluno@umc.br", claims.getSubject());
        assertEquals("Aluno", claims.get("role"));
        assertEquals(true, claims.get("termo"));
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void rejeitaChaveCurta() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService("chave-curta", 60_000)
        );
        assertEquals("JWT_SECRET deve ter pelo menos 32 bytes.", exception.getMessage());
    }

    @Test
    void geraTokenSemAceiteDoTermo() {
        JwtService service = new JwtService("12345678901234567890123456789012", 60_000);
        Claims claims = service.parse(service.generate("professor@umc.br", "Professor", false));

        assertFalse(claims.get("termo", Boolean.class));
    }
}
