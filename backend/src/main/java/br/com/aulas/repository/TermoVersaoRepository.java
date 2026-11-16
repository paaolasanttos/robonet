package br.com.aulas.repository;

import br.com.aulas.model.TermoVersao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermoVersaoRepository extends JpaRepository<TermoVersao, Integer> {
    boolean existsByVersao(String versao);
}
