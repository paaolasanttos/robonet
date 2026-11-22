package br.com.aulas.service;

import br.com.aulas.model.LogAuditoria;
import br.com.aulas.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditoriaServiceTest {
    @Test
    void registrarArmazenaDataHoraEmUtc() {
        LogAuditoriaRepository repository = mock(LogAuditoriaRepository.class);
        AuditoriaService service = new AuditoriaService(repository);
        LocalDateTime inicio = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1);

        service.registrar("usuario@example.com", "AUTENTICACAO", "LOGIN", "Login", true);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(repository).save(captor.capture());
        LocalDateTime registrada = captor.getValue().getDtRegistro();

        assertNotNull(registrada);
        assertFalse(registrada.isBefore(inicio));
        assertFalse(registrada.isAfter(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1)));
    }
}
