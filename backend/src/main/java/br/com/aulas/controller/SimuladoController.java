package br.com.aulas.controller;

import br.com.aulas.dto.SimuladoRequest;
import br.com.aulas.dto.SimuladoResponse;
import br.com.aulas.service.SimuladoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/simulados")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
public class SimuladoController {
    private final SimuladoService service;

    public SimuladoController(SimuladoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SimuladoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PostMapping
    public ResponseEntity<SimuladoResponse> cadastrar(@Valid @RequestBody SimuladoRequest dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(dados));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<SimuladoResponse> atualizar(@PathVariable Integer id, @Valid @RequestBody SimuladoRequest dados) {
        return ResponseEntity.ok(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
