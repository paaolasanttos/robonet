package br.com.aulas.repository;

import br.com.aulas.model.AulasAluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AulasAlunoRepository extends JpaRepository<AulasAluno, Long> {
    boolean existsByIdUsuarioAndIdAulaTema(Long idUsuario, Integer idAulaTema);

    Optional<AulasAluno> findByIdUsuarioAndIdAula(Long idUsuario, Long idAula);

    Optional<AulasAluno> findByIdUsuarioAndIdAulaTema(Long idUsuario, Integer idAulaTema);

    List<AulasAluno> findByIdUsuario(Long idUsuario);
}
