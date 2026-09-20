package com.robonet.admin.controller;

import com.robonet.admin.dto.UsuarioDTO;
import com.robonet.admin.model.Usuario;
import com.robonet.admin.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repo;


    @GetMapping
    public List<Usuario> listar() {
        return repo.findAll();
    }


    @PostMapping
    public ResponseEntity<?> criar(@RequestBody UsuarioDTO dto) {

        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(erro("O nome é obrigatório."));
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(erro("O e-mail é obrigatório."));
        }
        if (dto.getPerfil() == null) {
            return ResponseEntity.badRequest().body(erro("O perfil é obrigatório."));
        }

        if (!dto.getPerfil().equals("aluno") && !dto.getPerfil().equals("professor") && !dto.getPerfil().equals("admin")) {
            return ResponseEntity.badRequest().body(erro("Perfil inválido."));
        }
        if (dto.getPerfil().equals("aluno") && (dto.getRgm() == null || dto.getRgm().trim().isEmpty())) {
            return ResponseEntity.badRequest().body(erro("RGM é obrigatório para aluno."));
        }


        if (repo.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body(erro("Já existe um usuário com esse e-mail."));
        }

        Usuario u = new Usuario();
        u.setNome(dto.getNome());
        u.setEmail(dto.getEmail());
        u.setPerfil(dto.getPerfil());
        if (dto.getPerfil().equals("aluno")) {
            u.setRgm(dto.getRgm());
        } else {
            u.setRgm(null);
        }
        u.setAtivo(true);

        Usuario salvo = repo.save(u);
        return ResponseEntity.ok(salvo);
    }


    @PutMapping("/{id}/perfil")
    public ResponseEntity<?> mudarPerfil(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String novoPerfil = body.get("perfil");
        if (novoPerfil == null) {
            return ResponseEntity.badRequest().body(erro("Campo 'perfil' é obrigatório."));
        }
        if (!novoPerfil.equals("aluno") && !novoPerfil.equals("professor") && !novoPerfil.equals("admin")) {
            return ResponseEntity.badRequest().body(erro("Perfil inválido."));
        }

        Optional<Usuario> encontrado = repo.findById(id);
        if (encontrado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario u = encontrado.get();
        u.setPerfil(novoPerfil);
        if (!novoPerfil.equals("aluno")) {
            u.setRgm(null);
        }
        repo.save(u);
        return ResponseEntity.ok(u);
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<?> mudarStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean ativo = body.get("ativo");
        if (ativo == null) {
            return ResponseEntity.badRequest().body(erro("Campo 'ativo' é obrigatório."));
        }

        Optional<Usuario> encontrado = repo.findById(id);
        if (encontrado.isEmpty()) return ResponseEntity.notFound().build();

        Usuario u = encontrado.get();
        u.setAtivo(ativo);
        repo.save(u);
        return ResponseEntity.ok(u);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    private Map<String, String> erro(String msg) {
        Map<String, String> m = new HashMap<>();
        m.put("message", msg);
        return m;
    }
}
