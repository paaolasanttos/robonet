package br.com.aulas.repository;

import br.com.aulas.model.SimuladoRealizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SimuladoRealizacaoRepository extends JpaRepository<SimuladoRealizacao, Long> {
    long countByIdUsuario(Long idUsuario);

    @Query(value = """
            SELECT COUNT(*)
              FROM simuladosrealizacoes sr
              JOIN usuarios u ON u.idusuario = sr.idusuario
             WHERE LOWER(u.perfil) = 'aluno'
               AND LOWER(u.status) = 'ativo'
            """, nativeQuery = true)
    long countByAlunosAtivos();
}
