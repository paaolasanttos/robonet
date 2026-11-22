package br.com.aulas.repository;

import br.com.aulas.model.TorneioEquipe;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TorneioEquipeRepository extends JpaRepository<TorneioEquipe, Long> {
    List<TorneioEquipe> findByIdTorneio(Long idTorneio);

    List<TorneioEquipe> findByIdEquipe(Long idEquipe);

    boolean existsByIdEquipe(Long idEquipe);

    long countByIdTorneio(Long idTorneio);

    @Transactional
    @Modifying
    @Query("DELETE FROM TorneioEquipe t WHERE t.idTorneio = :idTorneio")
    void deleteByIdTorneio(@Param("idTorneio") Long idTorneio);
}
