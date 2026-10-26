package br.com.aulas.controller;

import br.com.aulas.dto.SimuladoAlunoResponse;
import br.com.aulas.dto.SimuladoRespostaRequest;
import br.com.aulas.dto.SimuladoResultadoResponse;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.service.SimuladoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulados-aluno")
@PreAuthorize("hasRole('ALUNO') and hasAuthority('TERMO_ACEITO')")
public class SimuladosAlunoController {
    private final SimuladoService service;
    private final UsuarioRepository usuarioRepository;

    public SimuladosAlunoController(SimuladoService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<SimuladoAlunoResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(service.listarParaAluno(usuarioAtual(authentication).getId()));
    }

    @PostMapping("/{id:\\d+}/responder")
    public ResponseEntity<SimuladoResultadoResponse> responder(
            @PathVariable Integer id,
            @Valid @RequestBody SimuladoRespostaRequest resposta,
            Authentication authentication) {
        return ResponseEntity.ok(service.corrigir(usuarioAtual(authentication).getId(), id, resposta));
    }

    private Usuario usuarioAtual(Authentication authentication) {
        return usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }
}
