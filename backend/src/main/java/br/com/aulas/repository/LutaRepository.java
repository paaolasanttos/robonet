package br.com.aulas.repository;

import br.com.aulas.model.Luta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LutaRepository extends JpaRepository<Luta, Long> {
    List<Luta> findByIdTorneioOrderByFaseAscOrdemAsc(Long idTorneio);

    Optional<Luta> findByIdTorneioAndFaseAndOrdem(Long idTorneio, Integer fase, Integer ordem);
}
