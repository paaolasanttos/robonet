package br.com.aulas.service;

import br.com.aulas.dto.SimuladoRequest;
import br.com.aulas.dto.SimuladoResponse;
import br.com.aulas.model.AulaTema;
import br.com.aulas.repository.AulaTemaRepository;
import br.com.aulas.repository.AulasAlunoRepository;
import br.com.aulas.repository.SimuladoRealizacaoRepository;
import br.com.aulas.repository.SimuladoRealizadoRepository;
import br.com.aulas.repository.SimuladoRepository;
import br.com.aulas.security.DatabaseUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimuladoServiceTest {
    @Mock SimuladoRepository simuladoRepository;
    @Mock SimuladoRealizacaoRepository realizacaoRepository;
    @Mock SimuladoRealizadoRepository realizadoRepository;
    @Mock AulaTemaRepository temaRepository;
    @Mock AulasAlunoRepository aulasAlunoRepository;
    @Mock DatabaseUserContext databaseUserContext;
    @Mock AuditoriaService auditoriaService;
    SimuladoService service;

    @BeforeEach
    void setup() {
        service = new SimuladoService(simuladoRepository, realizacaoRepository, realizadoRepository,
                temaRepository, aulasAlunoRepository, databaseUserContext, auditoriaService);
    }

    @Test
    void cadastrarExigeExatamenteUmaRespostaCorreta() {
        AulaTema tema = new AulaTema();
        tema.setId(1);
        tema.setTema("Lógica");
        SimuladoRequest request = new SimuladoRequest();
        request.setTemaId(1);
        request.setTitulo("Simulado");
        SimuladoRequest.PerguntaRequest pergunta = new SimuladoRequest.PerguntaRequest();
        pergunta.setEnunciado("Pergunta");
        SimuladoRequest.OpcaoRequest opcao = new SimuladoRequest.OpcaoRequest();
        opcao.setTexto("A");
        opcao.setCorreta(false);
        pergunta.setOpcoes(List.of(opcao));
        request.setPerguntas(List.of(pergunta));
        when(temaRepository.findById(1)).thenReturn(Optional.of(tema));

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(request));
        verify(simuladoRepository, never()).save(any());
    }
}
