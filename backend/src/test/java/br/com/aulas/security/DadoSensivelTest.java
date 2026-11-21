package br.com.aulas.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DadoSensivelTest {

    @Test
    void mascaraEmailMantendoApenasOsCincoPrimeirosCaracteres() {
        assertEquals("paola**************", DadoSensivel.mascararEmailComPrefixo("paola.santos@umc.br"));
        assertEquals("abcde**", DadoSensivel.mascararEmailComPrefixo("abcde@x"));
    }
}
