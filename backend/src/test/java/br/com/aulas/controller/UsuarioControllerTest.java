package br.com.aulas.controller;

import br.com.aulas.dto.UsuarioDTO;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.EquipeMembroRepository;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.DatabaseUserContext;
import br.com.aulas.service.AuditoriaService;
import br.com.aulas.service.ExclusaoContaService;
import br.com.aulas.service.SolicitacaoAlteracaoService;
import br.com.aulas.service.SolicitacaoCadastroService;
import br.com.aulas.service.TermoService;
import br.com.aulas.service.TurmaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UsuarioControllerTest {
    UsuarioRepository repo;
    PasswordEncoder passwordEncoder;
    DatabaseUserContext databaseUserContext;
    AuditoriaService auditoriaService;
    TurmaService turmaService;
    EquipeMembroRepository equipeMembroRepository;
    UsuarioController controller;

    @BeforeEach
    void setup() {
        repo = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        databaseUserContext = mock(DatabaseUserContext.class);
        auditoriaService = mock(AuditoriaService.class);
        turmaService = mock(TurmaService.class);
        equipeMembroRepository = mock(EquipeMembroRepository.class);
        controller = new UsuarioController();
        setField("repo", repo);
        setField("passwordEncoder", passwordEncoder);
        setField("databaseUserContext", databaseUserContext);
        setField("auditoriaService", auditoriaService);
        setField("turmaService", turmaService);
        setField("equipeMembroRepository", equipeMembroRepository);
        setField("termoService", mock(TermoService.class));
        setField("solicitacaoAlteracaoService", mock(SolicitacaoAlteracaoService.class));
        setField("solicitacaoCadastroService", mock(SolicitacaoCadastroService.class));
        setField("exclusaoContaService", mock(ExclusaoContaService.class));
    }

    private void setField(String name, Object value) {
        try {
            Field field = UsuarioController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void criarValidaNomeObrigatorio() {
        UsuarioDTO dto = new UsuarioDTO();

        ResponseEntity<?> response = controller.criar(dto);

        assertEquals(400, response.getStatusCode().value());
        verify(repo, never()).save(any(Usuario.class));
    }

    @Test
    void excluirDesativaUsuarioEmVezDeRemover() {
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("aluno@umc.br");
        usuario.setAtivo(true);
        when(repo.findById(10L)).thenReturn(Optional.of(usuario));

        ResponseEntity<?> response = controller.excluir(10L);

        assertEquals(204, response.getStatusCode().value());
        assertEquals(false, usuario.getAtivo());
        verify(repo).save(usuario);
    }
}
