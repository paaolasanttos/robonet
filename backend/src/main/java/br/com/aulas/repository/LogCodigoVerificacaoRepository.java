package br.com.aulas.repository;

import br.com.aulas.model.LogCodigoVerificacao;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LogCodigoVerificacaoRepository extends JpaRepository<LogCodigoVerificacao, Long> {
    Optional<LogCodigoVerificacao> findTopByEmailIgnoreCaseAndTipoOrderByDtGeracaoDesc(String email, String tipo);

    @Transactional
    @Modifying
    @Query("DELETE FROM LogCodigoVerificacao l WHERE LOWER(l.email) = LOWER(:email) AND l.tipo = :tipo")
    void deleteByEmailIgnoreCaseAndTipo(@Param("email") String email, @Param("tipo") String tipo);
}
