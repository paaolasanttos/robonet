package br.com.aulas.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SenhaPoliticaTest {

    @Test
    void aceitaSenhaQueAtendeTodosOsRequisitos() {
        assertTrue(SenhaPolitica.valida("Senha@123"));
        assertTrue(SenhaPolitica.valida("A1!bcdef"));
    }

    @Test
    void rejeitaSenhaForaDosRequisitos() {
        assertFalse(SenhaPolitica.valida(null));
        assertFalse(SenhaPolitica.valida(""));
        assertFalse(SenhaPolitica.valida("senha@123"));
        assertFalse(SenhaPolitica.valida("SENHA@123"));
        assertFalse(SenhaPolitica.valida("Senha1234"));
        assertFalse(SenhaPolitica.valida("S@1abc"));
        assertFalse(SenhaPolitica.valida("Senha@123 comEspaco"));
    }
}
