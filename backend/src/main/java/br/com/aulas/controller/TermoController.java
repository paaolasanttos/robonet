package br.com.aulas.controller;

import br.com.aulas.dto.AuthResponse;
import br.com.aulas.dto.TermoAtualResponse;
import br.com.aulas.dto.TermoPublicarRequest;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.JwtService;
import br.com.aulas.service.TermoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/termo")
public class TermoController {

    private final TermoService termoService;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public TermoController(TermoService termoService, UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.termoService = termoService;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @GetMapping("/atual")
    public ResponseEntity<TermoAtualResponse> atual() {
        return ResponseEntity.ok(termoService.atual());
    }

    @GetMapping("/versoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TermoAtualResponse>> versoes() {
        return ResponseEntity.ok(termoService.versoes());
    }

    @PostMapping("/publicar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TermoAtualResponse> publicar(@RequestBody TermoPublicarRequest request) {
        return ResponseEntity.ok(termoService.publicar(request.versao(), request.conteudo()));
    }

    @PostMapping("/aceitar")
    public ResponseEntity<?> aceitar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(authentication.getName()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Usuário não encontrado."));
        }

        termoService.aceitar(usuario);
        String token = jwtService.generate(usuario.getEmail(), usuario.getPerfil(), true);
        var resumo = new AuthResponse.UsuarioResumo(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(), true);
        return ResponseEntity.ok(new AuthResponse(token, resumo));
    }

    @PostMapping("/recusar")
    public ResponseEntity<?> recusar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(authentication.getName()).orElse(null);
        if (usuario != null) {
            termoService.recusar(usuario);
        }
        return ResponseEntity.noContent().build();
    }
}
