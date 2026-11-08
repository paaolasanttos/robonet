package br.com.aulas.controller;

import br.com.aulas.dto.TorneioDetalheResponse;
import br.com.aulas.dto.TorneioRequest;
import br.com.aulas.dto.TorneioResponse;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.service.TorneioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/torneios")
public class TorneioController {

    private final TorneioService service;
    private final UsuarioRepository usuarioRepository;

    public TorneioController(TorneioService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<TorneioResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(service.listar(usuarioLogado(authentication)));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<TorneioDetalheResponse> detalhar(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(service.detalhar(usuarioLogado(authentication), id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<TorneioResponse> cadastrar(@Valid @RequestBody TorneioRequest torneio, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(usuarioLogado(authentication), torneio));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<TorneioResponse> atualizar(@PathVariable Long id, @Valid @RequestBody TorneioRequest torneio,
                                                     Authentication authentication) {
        return ResponseEntity.ok(service.atualizar(usuarioLogado(authentication), id, torneio));
    }

    @PutMapping("/{id:\\d+}/equipes")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<TorneioDetalheResponse> definirEquipes(@PathVariable Long id, @RequestBody Map<String, List<Long>> body,
                                                                 Authentication authentication) {
        return ResponseEntity.ok(service.definirEquipes(usuarioLogado(authentication), id, body.get("equipes")));
    }

    @PostMapping("/{id:\\d+}/chaveamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<TorneioDetalheResponse> gerarChaveamento(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(service.gerarChaveamento(usuarioLogado(authentication), id));
    }

    @PutMapping("/lutas/{idLuta:\\d+}/vencedor")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<TorneioDetalheResponse> registrarVencedor(@PathVariable Long idLuta, @RequestBody Map<String, Long> body,
                                                                    Authentication authentication) {
        return ResponseEntity.ok(service.registrarVencedor(usuarioLogado(authentication), idLuta, body.get("equipeId")));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private Usuario usuarioLogado(Authentication authentication) {
        return usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }
}
