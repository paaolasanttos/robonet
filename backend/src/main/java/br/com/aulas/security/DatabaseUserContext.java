package br.com.aulas.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseUserContext {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseUserContext(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Não foi possível identificar o usuário autenticado.");
        }

        jdbcTemplate.queryForObject(
                "SELECT set_config('app.usuario', ?, true)",
                String.class,
                authentication.getName()
        );
    }
}