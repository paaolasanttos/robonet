package br.com.aulas.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusUsuarioConverterTest {

    private final StatusUsuarioConverter converter = new StatusUsuarioConverter();

    @Test
    void converteBooleanoParaStatusDoBanco() {
        assertEquals("Ativo", converter.convertToDatabaseColumn(true));
        assertEquals("Inativo", converter.convertToDatabaseColumn(false));
        assertEquals("Inativo", converter.convertToDatabaseColumn(null));
    }

    @Test
    void converteStatusDoBancoParaBooleanoIgnorandoMaiusculas() {
        assertTrue(converter.convertToEntityAttribute("ATIVO"));
        assertTrue(converter.convertToEntityAttribute("Ativo"));
        assertFalse(converter.convertToEntityAttribute("Inativo"));
        assertFalse(converter.convertToEntityAttribute(null));
    }
}
