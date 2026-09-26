package br.com.aulas.controller;

import br.com.aulas.dto.AulaRequest;
import br.com.aulas.dto.AulaResponse;
import br.com.aulas.model.AulaTema;
import br.com.aulas.service.AulaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aulas")
public class AulaController {

    private final AulaService service;

    public AulaController(AulaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AulaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<AulaResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/temas")
    public ResponseEntity<List<AulaTema>> listarTemas() {
        return ResponseEntity.ok(service.listarTemas());
    }

    @PostMapping
    public ResponseEntity<AulaResponse> cadastrar(@Valid @RequestBody AulaRequest aula) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(aula));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<AulaResponse> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody AulaRequest aula) {
        return ResponseEntity.ok(service.atualizar(id, aula));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}