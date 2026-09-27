package br.com.aulas.repository;

import br.com.aulas.model.TermoAceite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermoAceiteRepository extends JpaRepository<TermoAceite, Long> {
    boolean existsByIdUsuarioAndVersao(Long idUsuario, String versao);
}
