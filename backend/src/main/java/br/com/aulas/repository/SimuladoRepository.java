package br.com.aulas.repository;

import br.com.aulas.model.Simulado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SimuladoRepository extends JpaRepository<Simulado, Integer> {
    @Override
    @EntityGraph(attributePaths = "tema")
    List<Simulado> findAll();

    @Override
    @EntityGraph(attributePaths = "tema")
    Optional<Simulado> findById(Integer id);

    @EntityGraph(attributePaths = "tema")
    List<Simulado> findByTemaIdIn(List<Integer> temasIds);
}
