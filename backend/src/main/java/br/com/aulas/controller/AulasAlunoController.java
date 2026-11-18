package br.com.aulas.controller;

import br.com.aulas.dto.AulaResponse;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.service.AulasAlunoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aulas-aluno")
@PreAuthorize("hasRole('ALUNO') and hasAuthority('TERMO_ACEITO')")
public class AulasAlunoController {

    private final AulasAlunoService aulasAlunoService;
    private final UsuarioRepository usuarioRepository;

    public AulasAlunoController(AulasAlunoService aulasAlunoService, UsuarioRepository usuarioRepository) {
        this.aulasAlunoService = aulasAlunoService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<AulaResponse>> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return ResponseEntity.ok(aulasAlunoService.listarPorUsuario(usuario.getId()));
    }

    @PostMapping("/concluir")
    public ResponseEntity<Map<String, String>> concluir(@RequestBody Map<String, Integer> payload, Authentication authentication) {
        Integer aulaId = payload.get("aulaId");
        if (aulaId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "A aula informada é inválida."));
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        aulasAlunoService.concluir(usuario.getId(), aulaId);
        return ResponseEntity.ok(Map.of("message", "Aula marcada como concluída."));
    }
}
