package br.com.aulas.service;

import br.com.aulas.dto.TorneioRequest;
import br.com.aulas.model.Torneio;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.EquipeRepository;
import br.com.aulas.repository.LutaRepository;
import br.com.aulas.repository.TorneioEquipeRepository;
import br.com.aulas.repository.TorneioRepository;
import br.com.aulas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TorneioServiceTest {
    @Mock TorneioRepository torneioRepository;
    @Mock TorneioEquipeRepository torneioEquipeRepository;
    @Mock LutaRepository lutaRepository;
    @Mock EquipeRepository equipeRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock TurmaService turmaService;
    @Mock AuditoriaService auditoriaService;
    TorneioService service;

    @BeforeEach
    void setup() {
        service = new TorneioService(torneioRepository, torneioEquipeRepository, lutaRepository,
                equipeRepository, usuarioRepository, turmaService, auditoriaService);
    }

    @Test
    void cadastrarExigeDataDeInicioHoje() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        TorneioRequest request = new TorneioRequest();
        request.setNome("Torneio");
        request.setDtInicio(LocalDate.now().plusDays(1));
        request.setDtFim(LocalDate.now().plusDays(2));

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(usuario, request));
        verify(torneioRepository, never()).save(any(Torneio.class));
    }

    @Test
    void cadastrarExigeFimPosteriorAoInicio() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        TorneioRequest request = new TorneioRequest();
        request.setNome("Torneio");
        request.setDtInicio(LocalDate.now());
        request.setDtFim(LocalDate.now());

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(usuario, request));
        verify(torneioRepository, never()).save(any(Torneio.class));
    }
}
