package br.com.aulas.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
}
