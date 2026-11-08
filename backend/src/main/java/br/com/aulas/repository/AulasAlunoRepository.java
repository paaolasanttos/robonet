package br.com.aulas.repository;

import br.com.aulas.model.AulasAluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AulasAlunoRepository extends JpaRepository<AulasAluno, Long> {
    boolean existsByIdUsuarioAndIdAulaTema(Long idUsuario, Integer idAulaTema);

    Optional<AulasAluno> findByIdUsuarioAndIdAula(Long idUsuario, Long idAula);

    Optional<AulasAluno> findByIdUsuarioAndIdAulaTema(Long idUsuario, Integer idAulaTema);

    List<AulasAluno> findByIdUsuario(Long idUsuario);

    long countByIdUsuarioAndConcluidaIgnoreCase(Long idUsuario, String concluida);

    @Query(value = """
            SELECT COUNT(*)
              FROM aulasaluno aa
              JOIN usuarios u ON u.idusuario = aa.idusuario
             WHERE UPPER(aa.concluida) = 'S'
               AND LOWER(u.perfil) = 'aluno'
               AND LOWER(u.status) = 'ativo'
            """, nativeQuery = true)
    long countConcluidasPorAlunosAtivos();
}
