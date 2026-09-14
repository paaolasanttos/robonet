package br.com.aulas.service;

import br.com.aulas.dto.AulaRequest;
import br.com.aulas.dto.AulaResponse;
import br.com.aulas.exception.AulaNotFoundException;
import br.com.aulas.model.Aula;
import br.com.aulas.model.AulaTema;
import br.com.aulas.repository.AulaRepository;
import br.com.aulas.repository.AulaTemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AulaService {

    private final AulaRepository aulaRepository;
    private final AulaTemaRepository temaRepository;

    public AulaService(AulaRepository aulaRepository, AulaTemaRepository temaRepository) {
        this.aulaRepository = aulaRepository;
        this.temaRepository = temaRepository;
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> listar() {
        return aulaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AulaResponse buscarPorId(Integer id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<AulaTema> listarTemas() {
        return temaRepository.findAll();
    }

    @Transactional
    public AulaResponse cadastrar(AulaRequest dados) {
        Aula aula = new Aula();
        aula.setTema(buscarTema(dados.getTemaId()));
        aula.setTitulo(dados.getTitulo());
        aula.setDescricao(dados.getDescricao());
        return toResponse(aulaRepository.save(aula));
    }

    @Transactional
    public AulaResponse atualizar(Integer id, AulaRequest dados) {
        Aula aula = buscarEntidade(id);
        aula.setTema(buscarTema(dados.getTemaId()));
        aula.setTitulo(dados.getTitulo());
        aula.setDescricao(dados.getDescricao());
        return toResponse(aulaRepository.save(aula));
    }

    @Transactional
    public void excluir(Integer id) {
        if (!aulaRepository.existsById(id)) {
            throw new AulaNotFoundException(id);
        }
        aulaRepository.deleteById(id);
    }

    private Aula buscarEntidade(Integer id) {
        return aulaRepository.findById(id).orElseThrow(() -> new AulaNotFoundException(id));
    }

    private AulaTema buscarTema(Integer temaId) {
        if (temaId == null) {
            throw new IllegalArgumentException("O tema selecionado não existe.");
        }
        return temaRepository.findById(temaId)
                .orElseThrow(() -> new IllegalArgumentException("O tema selecionado não existe."));
    }

    private AulaResponse toResponse(Aula aula) {
        AulaTema tema = aula.getTema();
        return new AulaResponse(
                aula.getId(),
                tema != null ? tema.getId() : null,
                tema != null ? tema.getTema() : null,
                aula.getTitulo(),
                aula.getDescricao()
        );
    }
}