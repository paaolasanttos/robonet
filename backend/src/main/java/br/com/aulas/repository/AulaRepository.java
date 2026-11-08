package br.com.aulas.repository;

import br.com.aulas.model.Aula;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AulaRepository extends JpaRepository<Aula, Integer> {

    @EntityGraph(attributePaths = "tema")
    List<Aula> findAll();

    @EntityGraph(attributePaths = "tema")
    Optional<Aula> findById(Integer id);
}