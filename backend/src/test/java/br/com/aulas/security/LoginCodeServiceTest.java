package br.com.aulas.security;

import br.com.aulas.model.LogCodigoVerificacao;
import br.com.aulas.repository.LogCodigoVerificacaoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginCodeServiceTest {

    @Test
    void deveGerarEValidarCodigoPersistido() {
        LogCodigoVerificacaoRepository repository = mock(LogCodigoVerificacaoRepository.class);
        LoginCodeService service = new LoginCodeService(repository, "secret-test", 600, 60);

        when(repository.findTopByEmailIgnoreCaseAndTipoOrderByDtGeracaoDesc("robonet.umc@gmail.com", "login"))
                .thenReturn(Optional.empty());

        String codigo = service.issue("robonet.umc@gmail.com");

        ArgumentCaptor<LogCodigoVerificacao> captor = ArgumentCaptor.forClass(LogCodigoVerificacao.class);
        verify(repository, times(1)).save(captor.capture());

        LogCodigoVerificacao persistido = captor.getValue();
        assertEquals(6, codigo.length());
        assertEquals("robonet.umc@gmail.com", persistido.getEmail());
        assertEquals("login", persistido.getTipo());
        assertEquals(codigo, persistido.getCodigo());

        when(repository.findTopByEmailIgnoreCaseAndTipoOrderByDtGeracaoDesc("robonet.umc@gmail.com", "login"))
                .thenReturn(Optional.of(persistido));

        assertTrue(service.verify("robonet.umc@gmail.com", codigo));
        verify(repository, times(1)).deleteByEmailIgnoreCaseAndTipo("robonet.umc@gmail.com", "login");
    }
}
