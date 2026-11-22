package br.com.aulas.controller;

import br.com.aulas.dto.SolicitacaoCadastroRequest;
import br.com.aulas.service.SolicitacaoCadastroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SolicitacaoCadastroController {

    private final SolicitacaoCadastroService service;

    public SolicitacaoCadastroController(SolicitacaoCadastroService service) {
        this.service = service;
    }

    @PostMapping("/auth/solicitar-cadastro")
    public ResponseEntity<?> solicitar(@RequestBody SolicitacaoCadastroRequest request) {
        service.solicitar(request);
        return ResponseEntity.status(201).body(Map.of("message",
                "Solicitação enviada. Você poderá entrar quando o administrador aprovar o cadastro."));
    }
}
