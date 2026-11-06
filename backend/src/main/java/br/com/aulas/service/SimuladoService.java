package br.com.aulas.service;

import br.com.aulas.dto.SimuladoAlunoResponse;
import br.com.aulas.dto.SimuladoRequest;
import br.com.aulas.dto.SimuladoResponse;
import br.com.aulas.dto.SimuladoRespostaRequest;
import br.com.aulas.dto.SimuladoResultadoResponse;
import br.com.aulas.exception.RegistroNaoEncontradoException;
import br.com.aulas.model.AulaTema;
import br.com.aulas.model.Simulado;
import br.com.aulas.model.SimuladoOpcao;
import br.com.aulas.model.SimuladoPergunta;
import br.com.aulas.model.SimuladoRealizacao;
import br.com.aulas.repository.AulaTemaRepository;
import br.com.aulas.repository.AulasAlunoRepository;
import br.com.aulas.repository.SimuladoRepository;
import br.com.aulas.repository.SimuladoRealizacaoRepository;
import br.com.aulas.security.DatabaseUserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SimuladoService {
    private final SimuladoRepository simuladoRepository;
    private final SimuladoRealizacaoRepository simuladosRealizacaoRepository;
    private final AulaTemaRepository temaRepository;
    private final AulasAlunoRepository aulasAlunoRepository;
    private final DatabaseUserContext databaseUserContext;
    private final AuditoriaService auditoriaService;

    public SimuladoService(SimuladoRepository simuladoRepository, SimuladoRealizacaoRepository simuladosRealizacaoRepository,
                           AulaTemaRepository temaRepository,
                           AulasAlunoRepository aulasAlunoRepository, DatabaseUserContext databaseUserContext,
                           AuditoriaService auditoriaService) {
        this.simuladoRepository = simuladoRepository;
        this.simuladosRealizacaoRepository = simuladosRealizacaoRepository;
        this.temaRepository = temaRepository;
        this.aulasAlunoRepository = aulasAlunoRepository;
        this.databaseUserContext = databaseUserContext;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<SimuladoResponse> listar() {
        return simuladoRepository.findAll().stream().map(SimuladoResponse::from).toList();
    }

    @Transactional
    public SimuladoResponse cadastrar(SimuladoRequest dados) {
        databaseUserContext.setCurrentUser();
        Simulado simulado = new Simulado();
        preencher(simulado, dados);
        Simulado salvo = simuladoRepository.save(simulado);
        auditoriaService.registrar("SIMULADOS", "SIMULADO_CRIADO",
                "Simulado " + salvo.getId() + " - " + salvo.getTitulo(), true);
        return SimuladoResponse.from(salvo);
    }

    @Transactional
    public SimuladoResponse atualizar(Integer id, SimuladoRequest dados) {
        Simulado simulado = buscar(id);
        preencher(simulado, dados);
        Simulado salvo = simuladoRepository.save(simulado);
        auditoriaService.registrar("SIMULADOS", "SIMULADO_ALTERADO",
                "Simulado " + id + " - " + salvo.getTitulo(), true);
        return SimuladoResponse.from(salvo);
    }

    @Transactional
    public void excluir(Integer id) {
        Simulado simulado = buscar(id);
        simuladoRepository.delete(simulado);
        auditoriaService.registrar("SIMULADOS", "SIMULADO_EXCLUIDO", "Simulado " + id + " excluído", true);
    }

    @Transactional(readOnly = true)
    public List<SimuladoAlunoResponse> listarParaAluno(Long usuarioId) {
        List<Integer> temasAcessiveis = aulasAlunoRepository.findByIdUsuario(usuarioId).stream()
                .map(registro -> registro.getIdAulaTema())
                .distinct()
                .toList();
        if (temasAcessiveis.isEmpty()) {
            return List.of();
        }
        return simuladoRepository.findByTemaIdIn(temasAcessiveis).stream()
                .map(SimuladoAlunoResponse::from)
                .toList();
    }

    @Transactional
    public SimuladoResultadoResponse corrigir(Long usuarioId, Integer simuladoId, SimuladoRespostaRequest resposta) {
        Simulado simulado = buscar(simuladoId);
        if (!aulasAlunoRepository.existsByIdUsuarioAndIdAulaTema(usuarioId, simulado.getTema().getId())) {
            throw new IllegalArgumentException("Este simulado não está vinculado a uma aula disponível para você.");
        }

        if (resposta.respostas().size() != simulado.getPerguntas().size()) {
            throw new IllegalArgumentException("Responda a todas as perguntas do simulado.");
        }

        Set<Integer> perguntasRespondidas = new HashSet<>();
        int acertos = 0;
        for (SimuladoRespostaRequest.Resposta item : resposta.respostas()) {
            if (!perguntasRespondidas.add(item.perguntaId())) {
                throw new IllegalArgumentException("Cada pergunta deve ter apenas uma resposta.");
            }
            SimuladoPergunta pergunta = simulado.getPerguntas().stream()
                    .filter(p -> p.getId().equals(item.perguntaId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("A pergunta informada não pertence a este simulado."));
            SimuladoOpcao opcao = pergunta.getOpcoes().stream()
                    .filter(o -> o.getId().equals(item.opcaoId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("A opção selecionada não pertence à pergunta."));
            if (opcao.isCorreta()) {
                acertos++;
            }
        }

        SimuladoResultadoResponse resultado = new SimuladoResultadoResponse(acertos, simulado.getPerguntas().size(),
                100.0 * acertos / simulado.getPerguntas().size());
        simuladosRealizacaoRepository.save(new SimuladoRealizacao(usuarioId, simuladoId, LocalDateTime.now()));
        return resultado;
    }

    private void preencher(Simulado simulado, SimuladoRequest dados) {
        AulaTema tema = temaRepository.findById(dados.getTemaId())
                .orElseThrow(() -> new IllegalArgumentException("O tema selecionado não existe."));
        simulado.setTema(tema);
        simulado.setTitulo(dados.getTitulo().trim());

        List<SimuladoPergunta> perguntas = new ArrayList<>();
        for (SimuladoRequest.PerguntaRequest perguntaDados : dados.getPerguntas()) {
            long corretas = perguntaDados.getOpcoes().stream()
                    .filter(SimuladoRequest.OpcaoRequest::isCorreta).count();
            if (corretas != 1) {
                throw new IllegalArgumentException("Cada pergunta deve ter exatamente uma opção correta.");
            }

            SimuladoPergunta pergunta = new SimuladoPergunta();
            pergunta.setEnunciado(perguntaDados.getEnunciado().trim());
            List<SimuladoOpcao> opcoes = perguntaDados.getOpcoes().stream().map(opcaoDados -> {
                SimuladoOpcao opcao = new SimuladoOpcao();
                opcao.setTexto(opcaoDados.getTexto().trim());
                opcao.setCorreta(opcaoDados.isCorreta());
                return opcao;
            }).toList();
            pergunta.substituirOpcoes(opcoes);
            perguntas.add(pergunta);
        }
        simulado.substituirPerguntas(perguntas);
    }

    private Simulado buscar(Integer id) {
        return simuladoRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Simulado não encontrado."));
    }
}
