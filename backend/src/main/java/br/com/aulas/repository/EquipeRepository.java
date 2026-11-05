package br.com.aulas.repository;

import br.com.aulas.model.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {
    List<Equipe> findAllByOrderByNomeAsc();

    List<Equipe> findByIdTurmaInOrderByNomeAsc(Collection<Long> idsTurmas);

    boolean existsByIdTurma(Long idTurma);

    boolean existsByIdTurmaAndNomeIgnoreCase(Long idTurma, String nome);

    boolean existsByIdTurmaAndNomeIgnoreCaseAndIdNot(Long idTurma, String nome, Long id);
}
