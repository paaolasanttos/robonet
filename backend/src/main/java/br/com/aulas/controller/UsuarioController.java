package br.com.aulas.controller;

import br.com.aulas.dto.UsuarioDTO;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.EquipeMembroRepository;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.DatabaseUserContext;
import br.com.aulas.service.AuditoriaService;
import br.com.aulas.service.TurmaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private TurmaService turmaService;

    @Autowired
    private EquipeMembroRepository equipeMembroRepository;


    @GetMapping
    public List<Usuario> listar() {
        List<Usuario> usuarios = repo.findAll();
        usuarios.forEach(this::carregarTurmas);
        return usuarios;
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
        if (perfil.equals("aluno") && dto.getTurmaId() != null) {
            turmaService.validarTurma(dto.getTurmaId());
        }
        u.setAtivo(true);

        databaseUserContext.setCurrentUser();
        Usuario salvo = repo.save(u);
        turmaService.salvarTurmasUsuario(salvo.getId(), turmasDoPerfil(perfil, dto));
        carregarTurmas(salvo);
        auditoriaService.registrar("USUARIOS", "USUARIO_CRIADO",
                "Usuário " + salvo.getId() + " (" + salvo.getEmail() + ") criado com perfil " + perfil, true);
        auditoriaService.registrar("CRIPTOGRAFIA", "SENHA_CRIPTOGRAFADA",
                "Senha do usuário " + salvo.getId() + " salva com hash BCrypt", true);
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

        Long novaTurma = perfil.equals("aluno") ? dto.getTurmaId() : null;
        if (novaTurma != null) {
            turmaService.validarTurma(novaTurma);
        }
        if (!Objects.equals(turmaService.turmaDoAluno(id), novaTurma) && equipeMembroRepository.existsByIdUsuario(id)) {
            return ResponseEntity.badRequest().body(erro("O aluno faz parte de uma equipe. Retire-o da equipe antes de trocar a turma ou o perfil."));
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
            auditoriaService.registrar("CRIPTOGRAFIA", "SENHA_ALTERADA",
                    "Senha do usuário " + id + " alterada e salva com hash BCrypt", true);
        }
        Usuario salvo = repo.save(usuario);
        turmaService.salvarTurmasUsuario(id, turmasDoPerfil(perfil, dto));
        carregarTurmas(salvo);
        auditoriaService.registrar("USUARIOS", "USUARIO_ALTERADO",
                "Usuário " + id + " (" + email + ") alterado, perfil " + perfil, true);
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
        if (!novoPerfil.equals("aluno") && equipeMembroRepository.existsByIdUsuario(id)) {
            return ResponseEntity.badRequest().body(erro("O aluno faz parte de uma equipe. Retire-o da equipe antes de mudar o perfil."));
        }
        String perfilAnterior = u.getPerfil();
        u.setPerfil(novoPerfil);
        if (!novoPerfil.equals("aluno")) {
            u.setRgm(null);
        }
        repo.save(u);
        if (!novoPerfil.equals(perfilAnterior)) {
            turmaService.salvarTurmasUsuario(id, null);
        }
        auditoriaService.registrar("USUARIOS", "PERFIL_ALTERADO", "Usuário " + id + " alterado para o perfil " + novoPerfil, true);
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
        auditoriaService.registrar("USUARIOS", ativo ? "USUARIO_ATIVADO" : "USUARIO_INATIVADO", "Usuário " + id + " (" + u.getEmail() + ")", true);
        return ResponseEntity.ok(u);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Optional<Usuario> encontrado = repo.findById(id);
        if (encontrado.isEmpty()) return ResponseEntity.notFound().build();
        Usuario usuario = encontrado.get();
        usuario.setAtivo(false);
        repo.save(usuario);
        auditoriaService.registrar("USUARIOS", "USUARIO_EXCLUIDO", "Usuário " + id + " (" + usuario.getEmail() + ") desativado", true);
        return ResponseEntity.noContent().build();
    }


    private void carregarTurmas(Usuario usuario) {
        if ("aluno".equals(usuario.getPerfil())) {
            usuario.setIdTurma(turmaService.turmaDoAluno(usuario.getId()));
        }
        if ("professor".equals(usuario.getPerfil())) {
            usuario.setTurmas(turmaService.idsTurmasDoUsuario(usuario.getId()));
        }
    }

    private List<Long> turmasDoPerfil(String perfil, UsuarioDTO dto) {
        if (perfil.equals("aluno")) {
            return dto.getTurmaId() == null ? null : List.of(dto.getTurmaId());
        }
        if (perfil.equals("professor")) {
            return dto.getTurmas();
        }
        return null;
    }

    private Map<String, String> erro(String msg) {
        Map<String, String> m = new HashMap<>();
        m.put("message", msg);
        return m;
    }
}
