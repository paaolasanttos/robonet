package br.com.aulas.repository;

import br.com.aulas.model.Torneio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TorneioRepository extends JpaRepository<Torneio, Long> {
    List<Torneio> findAllByOrderByDtInicioDescIdDesc();
}
