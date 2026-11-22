package br.com.aulas.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DadoSensivelTest {

    @Test
    void mascaraEmailMantendoApenasOsCincoPrimeirosCaracteres() {
        assertEquals("paola**************", DadoSensivel.mascararEmailComPrefixo("paola.santos@umc.br"));
        assertEquals("abcde**", DadoSensivel.mascararEmailComPrefixo("abcde@x"));
    }

    @Test
    void mascararEmailPreservaPrimeiroCaracterESufixoDoDominio() {
        assertEquals("p*****@u*****.br", DadoSensivel.mascararEmail("paola@umc.br"));
        assertEquals("p*****@g*****.com", DadoSensivel.mascararEmail(" paola@gmail.com "));
    }

    @Test
    void mascararEmailMantemValoresNulosOuEmBranco() {
        assertEquals(null, DadoSensivel.mascararEmail(null));
        assertEquals("", DadoSensivel.mascararEmail(""));
        assertEquals("s*****", DadoSensivel.mascararEmail("semArroba"));
    }
}
