package br.com.aulas.controller;

import br.com.aulas.dto.TurmaRequest;
import br.com.aulas.dto.TurmaResponse;
import br.com.aulas.service.TurmaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turmas")
@PreAuthorize("hasRole('ADMIN')")
public class TurmaController {
    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @GetMapping
    public ResponseEntity<List<TurmaResponse>> listar() {
        return ResponseEntity.ok(turmaService.listar());
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<List<TurmaResponse>> minhasTurmas(Authentication authentication) {
        return ResponseEntity.ok(turmaService.listarDoUsuario(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<TurmaResponse> cadastrar(@Valid @RequestBody TurmaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turmaService.cadastrar(request.nome()));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        turmaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
