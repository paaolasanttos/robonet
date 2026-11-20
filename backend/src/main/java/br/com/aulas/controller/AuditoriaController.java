package br.com.aulas.controller;

import br.com.aulas.dto.LogAlteracaoResponse;
import br.com.aulas.model.LogAuditoria;
import br.com.aulas.service.AuditoriaService;
import br.com.aulas.service.LogAlteracaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {

    private final AuditoriaService service;
    private final LogAlteracaoService logAlteracaoService;

    public AuditoriaController(AuditoriaService service, LogAlteracaoService logAlteracaoService) {
        this.service = service;
        this.logAlteracaoService = logAlteracaoService;
    }

    @GetMapping
    public ResponseEntity<List<LogAuditoria>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/alteracoes")
    public ResponseEntity<List<LogAlteracaoResponse>> listarAlteracoes() {
        return ResponseEntity.ok(logAlteracaoService.listar());
    }
}
