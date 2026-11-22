package br.com.aulas.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuService {
    private final JdbcTemplate jdbcTemplate;

    public MenuService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<String> listarPorPerfil(String perfil) {
        String perfilAlternativo = "admin".equals(perfil) ? "administrador" : perfil;
        return jdbcTemplate.queryForList(
                "SELECT menu FROM usuariosperfilacessos WHERE LOWER(TRIM(perfil)) IN (?, ?) "
                        + "ORDER BY idusuarioperfilacesso",
                String.class, perfil, perfilAlternativo);
    }
}
