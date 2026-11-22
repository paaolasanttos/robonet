package br.com.aulas.service;

import br.com.aulas.dto.TurmaRequest;
import br.com.aulas.dto.TurmaResponse;
import br.com.aulas.exception.RegistroNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {
    @Mock JdbcTemplate jdbcTemplate;
    @Mock AuditoriaService auditoriaService;
    TurmaService service;

    @BeforeEach
    void setup() {
        service = new TurmaService(jdbcTemplate, auditoriaService);
    }

    @Test
    void listarRetornaTurmasOrdenadas() {
        when(jdbcTemplate.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<TurmaResponse>>any()))
                .thenReturn(List.of(new TurmaResponse(1L, "Robótica")));

        List<TurmaResponse> resultado = service.listar();

        assertEquals(1, resultado.size());
        assertEquals("Robótica", resultado.get(0).nome());
    }

    @Test
    void excluirLancaErroQuandoTurmaNaoExiste() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq(99L))).thenReturn(List.of());
        when(jdbcTemplate.update(anyString(), eq(99L))).thenReturn(0);

        assertThrows(RegistroNaoEncontradoException.class, () -> service.excluir(99L));
        verify(auditoriaService, never()).registrar(anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    void idsTurmasDoUsuarioDelegamAoJdbcTemplate() {
        when(jdbcTemplate.queryForList(anyString(), eq(Long.class), eq(7L))).thenReturn(List.of(1L, 2L));

        assertEquals(List.of(1L, 2L), service.idsTurmasDoUsuario(7L));
    }

    @Test
    void idsUnicosEvitaDuplicadosAoSalvarTurmas() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), eq(3L))).thenReturn(true);
        when(jdbcTemplate.update(anyString(), eq(7L))).thenReturn(1);

        service.salvarTurmasUsuario(7L, List.of(3L, 3L));

        verify(jdbcTemplate, times(1)).update(eq("INSERT INTO usuariosturmas (idusuario, idturma) VALUES (?, ?)"), eq(7L), eq(3L));
    }
}
