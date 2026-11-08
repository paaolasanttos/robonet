package br.com.aulas.controller;

import br.com.aulas.dto.ExclusaoRequest;
import br.com.aulas.dto.MeusDadosExportResponse;
import br.com.aulas.dto.MeusDadosResponse;
import br.com.aulas.dto.SolicitacaoAlteracaoDadosRequest;
import br.com.aulas.dto.SenhaRequest;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.service.ExclusaoContaService;
import br.com.aulas.service.MeusDadosService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meus-dados")
@PreAuthorize("hasAnyRole('ALUNO', 'PROFESSOR')")
public class MeusDadosController {
    private final MeusDadosService service;
    private final ExclusaoContaService exclusaoContaService;
    private final UsuarioRepository usuarioRepository;

    public MeusDadosController(MeusDadosService service, ExclusaoContaService exclusaoContaService, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.exclusaoContaService = exclusaoContaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<MeusDadosResponse> consultar(Authentication authentication) {
        return ResponseEntity.ok(service.consultar(usuarioLogado(authentication)));
    }

    @PostMapping("/alteracao")
    public ResponseEntity<Void> solicitarAlteracao(Authentication authentication,
                                                   @Valid @RequestBody SolicitacaoAlteracaoDadosRequest request) {
        service.solicitarAlteracao(usuarioLogado(authentication), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(Authentication authentication, @Valid @RequestBody SenhaRequest request) {
        service.alterarSenha(usuarioLogado(authentication), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exportar")
    public ResponseEntity<MeusDadosExportResponse> exportar(Authentication authentication) {
        return ResponseEntity.ok(service.exportar(usuarioLogado(authentication)));
    }

    @PostMapping("/exclusao")
    public ResponseEntity<Void> solicitarExclusao(Authentication authentication, @Valid @RequestBody ExclusaoRequest request) {
        exclusaoContaService.solicitar(usuarioLogado(authentication), request.senhaAtual());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/exclusao")
    public ResponseEntity<Void> cancelarExclusao(Authentication authentication) {
        exclusaoContaService.cancelar(usuarioLogado(authentication));
        return ResponseEntity.noContent().build();
    }

    private Usuario usuarioLogado(Authentication authentication) {
        return usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }
}
