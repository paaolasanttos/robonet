package br.com.aulas.repository;

import br.com.aulas.model.SolicitacaoExclusao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SolicitacaoExclusaoRepository extends JpaRepository<SolicitacaoExclusao, Long> {
    Optional<SolicitacaoExclusao> findByIdUsuarioAndStatus(Long idUsuario, String status);

    List<SolicitacaoExclusao> findByStatusAndDtLimiteLessThanEqual(String status, LocalDateTime dtLimite);
}
