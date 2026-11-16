package br.com.aulas.security;

import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseUserContext {
    private final JdbcTemplate jdbcTemplate;
    private final UsuarioRepository usuarioRepository;

    public DatabaseUserContext(JdbcTemplate jdbcTemplate, UsuarioRepository usuarioRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.usuarioRepository = usuarioRepository;
    }

    public void setCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Não foi possível identificar o usuário autenticado.");
        }

        String email = authentication.getName();
        String nomeUsuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(email)
                .map(Usuario::getNome)
                .filter(nome -> !nome.isBlank())
                .orElse(email);

        jdbcTemplate.queryForObject(
                "SELECT set_config('app.usuario', ?, true)",
                String.class,
                nomeUsuario
        );
    }
}