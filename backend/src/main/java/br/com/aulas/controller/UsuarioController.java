package br.com.aulas.controller;

import br.com.aulas.dto.UsuarioDTO;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.DatabaseUserContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DatabaseUserContext databaseUserContext;


    @GetMapping
    public List<Usuario> listar() {
        return repo.findAll();
    }


    @PostMapping
    @Transactional
    public ResponseEntity<?> criar(@RequestBody UsuarioDTO dto) {

        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(erro("O nome é obrigatório."));
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(erro("O e-mail é obrigatório."));
        }
        if (dto.getSenha() == null || dto.getSenha().length() < 8) {
            return ResponseEntity.badRequest().body(erro("A senha deve ter pelo menos 8 caracteres."));
        }
        if (dto.getPerfil() == null) {
            return ResponseEntity.badRequest().body(erro("O perfil é obrigatório."));
        }

        String perfil = dto.getPerfil().trim().toLowerCase(Locale.ROOT);
        if (!perfil.equals("aluno") && !perfil.equals("professor") && !perfil.equals("admin")) {
            return ResponseEntity.badRequest().body(erro("Perfil inválido."));
        }
        if (perfil.equals("aluno") && (dto.getRgm() == null || dto.getRgm().trim().isEmpty())) {
            return ResponseEntity.badRequest().body(erro("RGM é obrigatório para aluno."));
        }


        String email = dto.getEmail().trim();
        if (repo.existsByEmailIgnoreCaseAndAtivoTrue(email)) {
            return ResponseEntity.badRequest().body(erro("Já existe um usuário com esse e-mail."));
        }

        Usuario u = new Usuario();
        u.setNome(dto.getNome().trim());
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode(dto.getSenha()));
        u.setPerfil(perfil);
        if (perfil.equals("aluno")) {
            u.setRgm(dto.getRgm().trim());
        } else {
            u.setRgm(null);
        }
        u.setAtivo(true);

        databaseUserContext.setCurrentUser();
        Usuario salvo = repo.save(u);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody UsuarioDTO dto) {
        Optional<Usuario> encontrado = repo.findById(id);
        if (encontrado.isEmpty()) return ResponseEntity.notFound().build();
        if (dto.getNome() == null || dto.getNome().trim().isEmpty()
                || dto.getEmail() == null || dto.getEmail().trim().isEmpty()
                || dto.getPerfil() == null || dto.getPerfil().isBlank()) {
            return ResponseEntity.badRequest().body(erro("Nome, e-mail e perfil são obrigatórios."));
        }

        String email = dto.getEmail().trim();
        String perfil = dto.getPerfil().trim().toLowerCase(Locale.ROOT);
        if (!perfil.equals("aluno") && !perfil.equals("professor") && !perfil.equals("admin")) {
            return ResponseEntity.badRequest().body(erro("Perfil inválido."));
        }
        if (perfil.equals("aluno") && (dto.getRgm() == null || dto.getRgm().trim().isEmpty())) {
            return ResponseEntity.badRequest().body(erro("RGM é obrigatório para aluno."));
        }
        Usuario usuario = encontrado.get();
        if (!usuario.getEmail().equalsIgnoreCase(email) && repo.existsByEmailIgnoreCaseAndAtivoTrue(email)) {
            return ResponseEntity.badRequest().body(erro("Já existe um usuário ativo com esse e-mail."));
        }

        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setPerfil(perfil);
        usuario.setRgm(perfil.equals("aluno") ? dto.getRgm().trim() : null);
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            if (dto.getSenha().length() < 8) {
                return ResponseEntity.badRequest().body(erro("A senha deve ter pelo menos 8 caracteres."));
            }
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        return ResponseEntity.ok(repo.save(usuario));
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
        Optional<Usuario> encontrado = repo.findById(id);
        if (encontrado.isEmpty()) return ResponseEntity.notFound().build();
        Usuario usuario = encontrado.get();
        usuario.setAtivo(false);
        repo.save(usuario);
        return ResponseEntity.noContent().build();
    }


    private Map<String, String> erro(String msg) {
        Map<String, String> m = new HashMap<>();
        m.put("message", msg);
        return m;
    }
}
