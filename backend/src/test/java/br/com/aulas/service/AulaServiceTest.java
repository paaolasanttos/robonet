package br.com.aulas.service;

import br.com.aulas.dto.AulaRequest;
import br.com.aulas.dto.AulaResponse;
import br.com.aulas.exception.AulaNotFoundException;
import br.com.aulas.model.Aula;
import br.com.aulas.model.AulaTema;
import br.com.aulas.repository.AulaRepository;
import br.com.aulas.repository.AulaTemaRepository;
import br.com.aulas.repository.AulasAlunoRepository;
import br.com.aulas.security.DatabaseUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulaServiceTest {
    @Mock AulaRepository aulaRepository;
    @Mock AulaTemaRepository temaRepository;
    @Mock AulasAlunoRepository aulasAlunoRepository;
    @Mock DatabaseUserContext databaseUserContext;
    @Mock AuditoriaService auditoriaService;
    AulaService service;

    @BeforeEach
    void setup() {
        service = new AulaService(aulaRepository, temaRepository, aulasAlunoRepository, databaseUserContext, auditoriaService);
    }

    @Test
    void cadastrarCriaAulaEResposta() {
        AulaTema tema = new AulaTema();
        tema.setId(2);
        tema.setTema("Sensores");
        AulaRequest request = new AulaRequest();
        request.setTemaId(2);
        request.setTitulo("Aula 1");
        request.setDescricao("Conteúdo");

        when(temaRepository.findById(2)).thenReturn(Optional.of(tema));
        when(aulaRepository.save(any(Aula.class))).thenAnswer(invocation -> {
            Aula aula = invocation.getArgument(0);
            aula.setId(10);
            return aula;
        });

        AulaResponse response = service.cadastrar(request);

        assertEquals(10, response.getId());
        assertEquals("Aula 1", response.getTitulo());
        verify(databaseUserContext).setCurrentUser();
        verify(auditoriaService).registrar(eq("AULAS"), eq("AULA_CRIADA"), contains("Aula 10"), eq(true));
    }

    @Test
    void buscarPorIdLancaErroQuandoNaoExiste() {
        when(aulaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(AulaNotFoundException.class, () -> service.buscarPorId(99));
    }

    @Test
    void excluirNaoChamaRepositorioQuandoInexistente() {
        when(aulaRepository.existsById(99)).thenReturn(false);

        assertThrows(AulaNotFoundException.class, () -> service.excluir(99));
        verify(aulaRepository, never()).deleteById(anyInt());
    }
}
