package com.robonet.admin.controller;

import com.robonet.admin.dto.PerfilRequest;
import com.robonet.admin.dto.StatusRequest;
import com.robonet.admin.dto.UsuarioRequest;
import com.robonet.admin.dto.UsuarioResponse;
import com.robonet.admin.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dados));
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<UsuarioResponse> alterarPerfil(@PathVariable Long id,
                                                         @Valid @RequestBody PerfilRequest dados) {
        return ResponseEntity.ok(service.alterarPerfil(id, dados.getPerfil()));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<UsuarioResponse> alterarStatus(@PathVariable Long id,
                                                         @Valid @RequestBody StatusRequest dados) {
        return ResponseEntity.ok(service.alterarStatus(id, dados.getAtivo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}