package br.com.aulas.service;

import br.com.aulas.dto.AulaResponse;
import br.com.aulas.model.Aula;
import br.com.aulas.model.AulaTema;
import br.com.aulas.model.AulasAluno;
import br.com.aulas.repository.AulaRepository;
import br.com.aulas.repository.AulasAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AulasAlunoService {

    private final AulasAlunoRepository aulasAlunoRepository;
    private final AulaRepository aulaRepository;

    public AulasAlunoService(AulasAlunoRepository aulasAlunoRepository, AulaRepository aulaRepository) {
        this.aulasAlunoRepository = aulasAlunoRepository;
        this.aulaRepository = aulaRepository;
    }

    @Transactional
    public AulasAluno concluir(Long idUsuario, Integer idAula) {
        Aula aula = aulaRepository.findById(idAula)
                .orElseThrow(() -> new IllegalArgumentException("Aula não encontrada."));

        Integer idAulaTema = aula.getTema() != null ? aula.getTema().getId() : null;
        if (idAulaTema == null) {
            throw new IllegalArgumentException("A aula informada não possui tema válido.");
        }

        AulasAluno registro = aulasAlunoRepository.findByIdUsuarioAndIdAula(idUsuario, aula.getId().longValue())
                .orElseGet(() -> {
                    AulasAluno novo = new AulasAluno();
                    novo.setIdAula(aula.getId().longValue());
                    novo.setIdUsuario(idUsuario);
                    novo.setIdAulaTema(idAulaTema);
                    return novo;
                });

        registro.setConcluida("S");
        return aulasAlunoRepository.save(registro);
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> listarPorUsuario(Long idUsuario) {
        Map<Integer, Boolean> conclusoes = aulasAlunoRepository.findByIdUsuario(idUsuario).stream()
                .filter(registro -> registro.getIdAula() != null)
                .collect(Collectors.toMap(
                        registro -> Math.toIntExact(registro.getIdAula()),
                        registro -> "S".equalsIgnoreCase(registro.getConcluida()),
                        (atual, proximo) -> atual || proximo
                ));

        return aulaRepository.findAll().stream()
                .map(aula -> toResponse(aula, conclusoes.getOrDefault(aula.getId(), false)))
                .sorted(Comparator.comparing(AulaResponse::getConcluida).reversed())
                .toList();
    }

    private AulaResponse toResponse(Aula aula) {
        return toResponse(aula, false);
    }

    private AulaResponse toResponse(Aula aula, boolean concluida) {
        AulaTema tema = aula.getTema();
        return new AulaResponse(
                aula.getId(),
                tema != null ? tema.getId() : null,
                tema != null ? tema.getTema() : null,
                aula.getTitulo(),
                aula.getDescricao(),
                concluida
        );
    }
}
