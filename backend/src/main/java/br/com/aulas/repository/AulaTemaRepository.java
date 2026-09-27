package br.com.aulas.repository;

import br.com.aulas.model.AulaTema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AulaTemaRepository extends JpaRepository<AulaTema, Integer> {
}