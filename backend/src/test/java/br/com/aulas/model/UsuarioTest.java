package br.com.aulas.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    @Test
    void antesDeSalvarMantemValoresAtivosPadrao() {
        Usuario usuario = new Usuario();

        usuario.antesDeSalvar();

        assertTrue(usuario.getAtivo());
        assertTrue("N".equals(usuario.getAcessoMFV()));
    }

    @Test
    void antesDeSalvarPreservaValoresInformados() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(false);
        usuario.setAcessoMFV("S");

        usuario.antesDeSalvar();

        assertFalse(usuario.getAtivo());
        assertTrue("S".equals(usuario.getAcessoMFV()));
    }

    @Test
    void antesDeSalvarInicializaValoresNulos() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(null);
        usuario.setAcessoMFV(null);

        usuario.antesDeSalvar();

        assertTrue(usuario.getAtivo());
        assertTrue("N".equals(usuario.getAcessoMFV()));
    }
}
