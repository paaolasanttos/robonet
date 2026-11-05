package br.com.aulas.service;

import br.com.aulas.dto.DashboardResumoResponse;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.AulasAlunoRepository;
import br.com.aulas.repository.SimuladoRealizacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final AulasAlunoRepository aulasAlunoRepository;
    private final SimuladoRealizacaoRepository simuladosRealizacaoRepository;

    public DashboardService(AulasAlunoRepository aulasAlunoRepository,
                            SimuladoRealizacaoRepository simuladosRealizacaoRepository) {
        this.aulasAlunoRepository = aulasAlunoRepository;
        this.simuladosRealizacaoRepository = simuladosRealizacaoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResumoResponse resumo(Usuario usuario) {
        if ("aluno".equalsIgnoreCase(usuario.getPerfil())) {
            return new DashboardResumoResponse(
                    aulasAlunoRepository.countByIdUsuarioAndConcluidaIgnoreCase(usuario.getId(), "S"),
                    simuladosRealizacaoRepository.countByIdUsuario(usuario.getId())
            );
        }

        return new DashboardResumoResponse(
                aulasAlunoRepository.countConcluidasPorAlunosAtivos(),
                simuladosRealizacaoRepository.countByAlunosAtivos()
        );
    }
}
