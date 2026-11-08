package br.com.aulas.repository;

import br.com.aulas.model.SimuladoRealizado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimuladoRealizadoRepository extends JpaRepository<SimuladoRealizado, Integer> {
    List<SimuladoRealizado> findByIdUsuarioOrderByDataTentativaDescTentativaDescIdSimuladoPerguntaAsc(Long idUsuario);

    boolean existsByIdUsuarioAndIdSimulado(Long idUsuario, Integer idSimulado);
}
