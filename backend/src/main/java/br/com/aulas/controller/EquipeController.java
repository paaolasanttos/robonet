package br.com.aulas.controller;

import br.com.aulas.dto.AlunoTurmaResponse;
import br.com.aulas.dto.EquipeRequest;
import br.com.aulas.dto.EquipeResponse;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.service.EquipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipes")
public class EquipeController {

    private final EquipeService service;
    private final UsuarioRepository usuarioRepository;

    public EquipeController(EquipeService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<EquipeResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(service.listar(usuarioLogado(authentication)));
    }

    @GetMapping("/alunos")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<AlunoTurmaResponse>> listarAlunos(@RequestParam Long turmaId, Authentication authentication) {
        return ResponseEntity.ok(service.listarAlunos(usuarioLogado(authentication), turmaId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<EquipeResponse> cadastrar(@Valid @RequestBody EquipeRequest equipe, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(usuarioLogado(authentication), equipe));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<EquipeResponse> atualizar(@PathVariable Long id, @Valid @RequestBody EquipeRequest equipe,
                                                    Authentication authentication) {
        return ResponseEntity.ok(service.atualizar(usuarioLogado(authentication), id, equipe));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> excluir(@PathVariable Long id, Authentication authentication) {
        service.excluir(usuarioLogado(authentication), id);
        return ResponseEntity.noContent().build();
    }

    private Usuario usuarioLogado(Authentication authentication) {
        return usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }
}
