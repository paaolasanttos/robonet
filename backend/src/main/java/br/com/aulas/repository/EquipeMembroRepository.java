package br.com.aulas.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EquipeMembroRepository {
    private final JdbcTemplate jdbcTemplate;

    public EquipeMembroRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByIdUsuario(Long idUsuario) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM equipesmembros WHERE idusuario = ?)",
                Boolean.class,
                idUsuario
        );
        return Boolean.TRUE.equals(exists);
    }

    public Long equipeDoUsuario(Long idUsuario) {
        return jdbcTemplate.queryForList(
                "SELECT idequipe FROM equipesmembros WHERE idusuario = ?",
                Long.class,
                idUsuario
        ).stream().findFirst().orElse(null);
    }

    public List<Long> idsUsuariosDaEquipe(Long idEquipe) {
        return jdbcTemplate.queryForList(
                "SELECT idusuario FROM equipesmembros WHERE idequipe = ? ORDER BY idequipemembro",
                Long.class,
                idEquipe
        );
    }

    public void salvar(Long idEquipe, Long idUsuario) {
        jdbcTemplate.update("INSERT INTO equipesmembros (idequipe, idusuario) VALUES (?, ?)", idEquipe, idUsuario);
    }

    public void excluirMembros(Long idEquipe) {
        jdbcTemplate.update("DELETE FROM equipesmembros WHERE idequipe = ?", idEquipe);
    }
}
