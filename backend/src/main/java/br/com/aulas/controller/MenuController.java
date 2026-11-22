package br.com.aulas.controller;

import br.com.aulas.service.MenuService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/menu")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'ALUNO')")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<String> listar(Authentication authentication) {
        String perfil = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()).toLowerCase(Locale.ROOT))
                .filter(role -> role.equals("admin") || role.equals("professor") || role.equals("aluno"))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Perfil sem acesso ao menu."));
        return menuService.listarPorPerfil(perfil);
    }
}
