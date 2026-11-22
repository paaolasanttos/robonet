package br.com.aulas.service;

import br.com.aulas.dto.EquipeRequest;
import br.com.aulas.model.Equipe;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.EquipeMembroRepository;
import br.com.aulas.repository.EquipeRepository;
import br.com.aulas.repository.TorneioEquipeRepository;
import br.com.aulas.repository.TorneioRepository;
import br.com.aulas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipeServiceTest {
    @Mock EquipeRepository equipeRepository;
    @Mock EquipeMembroRepository equipeMembroRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock TorneioEquipeRepository torneioEquipeRepository;
    @Mock TorneioRepository torneioRepository;
    @Mock TurmaService turmaService;
    @Mock AuditoriaService auditoriaService;
    EquipeService service;

    @BeforeEach
    void setup() {
        service = new EquipeService(equipeRepository, equipeMembroRepository, usuarioRepository,
                torneioEquipeRepository, torneioRepository, turmaService, auditoriaService);
    }

    @Test
    void cadastrarRecusaProfessorForaDaTurma() {
        Usuario professor = new Usuario();
        professor.setId(5L);
        professor.setPerfil("professor");
        EquipeRequest request = new EquipeRequest();
        request.setTurmaId(2L);
        request.setNome("Robôs");
        request.setMembros(List.of(1L));
        when(turmaService.idsTurmasDoUsuario(5L)).thenReturn(List.of(1L));

        assertThrows(AccessDeniedException.class, () -> service.cadastrar(professor, request));
        verify(equipeRepository, never()).save(any(Equipe.class));
    }

    @Test
    void cadastrarRecusaNomeDuplicadoNaMesmaTurma() {
        Usuario admin = new Usuario();
        admin.setPerfil("admin");
        EquipeRequest request = new EquipeRequest();
        request.setTurmaId(1L);
        request.setNome("Robôs");
        request.setMembros(List.of(1L));
        when(equipeRepository.existsByIdTurmaAndNomeIgnoreCase(1L, "Robôs")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(admin, request));
        verify(equipeRepository, never()).save(any(Equipe.class));
    }
}
