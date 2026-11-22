package br.com.aulas.controller;

import br.com.aulas.dto.DashboardResumoResponse;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'ALUNO')")
public class DashboardController {
    private final DashboardService dashboardService;
    private final UsuarioRepository usuarioRepository;

    public DashboardController(DashboardService dashboardService, UsuarioRepository usuarioRepository) {
        this.dashboardService = dashboardService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<DashboardResumoResponse> resumo(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return ResponseEntity.ok(dashboardService.resumo(usuario));
    }
}
