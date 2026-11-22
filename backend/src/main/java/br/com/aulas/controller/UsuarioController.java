package br.com.aulas.controller;

import br.com.aulas.dto.UsuarioDTO;
import br.com.aulas.dto.IndeferirSolicitacaoRequest;
import br.com.aulas.dto.SolicitacaoAlteracaoAnaliseResponse;
import br.com.aulas.dto.UsuarioListaResponse;
import br.com.aulas.dto.AnaliseCadastroRequest;
import br.com.aulas.dto.ExclusaoAnaliseResponse;
import br.com.aulas.dto.SolicitacaoCadastroResponse;
import br.com.aulas.service.ExclusaoContaService;
import br.com.aulas.service.SolicitacaoCadastroService;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.EquipeMembroRepository;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.DadoSensivel;
import br.com.aulas.security.DatabaseUserContext;
import br.com.aulas.security.SenhaPolitica;
import br.com.aulas.service.AuditoriaService;
import br.com.aulas.service.SolicitacaoAlteracaoService;
import br.com.aulas.service.TermoService;
import br.com.aulas.service.TurmaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Locale;
import java.util.Set;

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

    @Autowired
    private SolicitacaoAlteracaoService solicitacaoAlteracaoService;

    @Autowired
    private TermoService termoService;

    @Autowired
    private SolicitacaoCadastroService solicitacaoCadastroService;

    @Autowired
    private ExclusaoContaService exclusaoContaService;


    @GetMapping
    public List<UsuarioListaResponse> listar() {
        List<Usuario> usuarios = repo.findAll();
        usuarios.forEach(this::carregarTurmas);
        Set<Long> aceitaram = termoService.usuariosQueAceitaramVersaoAtual();
        return usuarios.stream()
                .map(usuario -> new UsuarioListaResponse(usuario.getId(), usuario.getNome(),
                        DadoSensivel.mascararEmailComPrefixo(usuario.getEmail()), usuario.getRa(), usuario.getPerfil(),
                        usuario.getAtivo(), usuario.getAcessoMFV(), usuario.getCriadoEm(), usuario.getIdTurma(),
                        usuario.getTurmas(),
                        "admin".equals(usuario.getPerfil()) ? null : aceitaram.contains(usuario.getId())))
                .toList();
    }

    @GetMapping("/{id}/email")
    public ResponseEntity<?> visualizarEmail(@PathVariable Long id) {
        Optional<Usuario> encontrado = repo.findById(id);
        if (encontrado.isEmpty()) return ResponseEntity.notFound().build();
        auditoriaService.registrar("LGPD", "DADO_SENSIVEL_VISUALIZADO",
                "E-mail do usuário " + id + " visualizado pelo administrador", true);
        return ResponseEntity.ok(Map.of("email", encontrado.get().getEmail()));
    }

    @GetMapping("/solicitacoes")
    public List<SolicitacaoAlteracaoAnaliseResponse> listarSolicitacoes() {
        return solicitacaoAlteracaoService.listar();
    }

    @GetMapping("/solicitacoes/{id}")
    public SolicitacaoAlteracaoAnaliseResponse detalharSolicitacao(@PathVariable Long id) {
        return solicitacaoAlteracaoService.detalhar(id);
    }

    @GetMapping("/cadastros")
    public List<SolicitacaoCadastroResponse> listarCadastros() {
        return solicitacaoCadastroService.listar();
    }

    @GetMapping("/cadastros/{id}")
    public SolicitacaoCadastroResponse detalharCadastro(@PathVariable Long id) {
        return solicitacaoCadastroService.detalhar(id);
    }

    @PostMapping("/cadastros/{id}/aprovar")
    public ResponseEntity<Void> aprovarCadastro(@PathVariable Long id, Authentication authentication,
                                                @RequestBody(required = false) AnaliseCadastroRequest request) {
        solicitacaoCadastroService.aprovar(id, request == null ? null : request.turmaId(), authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cadastros/{id}/recusar")
    public ResponseEntity<Void> recusarCadastro(@PathVariable Long id, Authentication authentication,
                                                @RequestBody AnaliseCadastroRequest request) {
        solicitacaoCadastroService.recusar(id, request.motivo(), authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exclusoes")
    public List<ExclusaoAnaliseResponse> listarExclusoes() {
        return exclusaoContaService.listarParaAnalise();
    }

    @PostMapping("/exclusoes/{id}/aprovar")
    public ResponseEntity<Void> aprovarExclusao(@PathVariable Long id, Authentication authentication) {
        exclusaoContaService.aprovar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/solicitacoes/{id}/deferir")
    public ResponseEntity<Void> deferirSolicitacao(@PathVariable Long id, Authentication authentication) {
        solicitacaoAlteracaoService.deferir(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/solicitacoes/{id}/indeferir")
    public ResponseEntity<Void> indeferirSolicitacao(@PathVariable Long id, Authentication authentication,
                                                     @Valid @RequestBody IndeferirSolicitacaoRequest request) {
        solicitacaoAlteracaoService.indeferir(id, authentication.getName(), request.motivo());
        return ResponseEntity.noContent().build();
    }


    @PostMapping
    @Transactional
    public ResponseEntity<?> criar(@RequestBody UsuarioDTO dto) {

        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(erro("O nome é obrigatório."));
        }
        if (dto.getNome().trim().length() > 50) {
            return ResponseEntity.badRequest().body(erro("O nome deve ter no máximo 50 caracteres."));
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(erro("O e-mail é obrigatório."));
        }
        if (!SenhaPolitica.valida(dto.getSenha())) {
            return ResponseEntity.badRequest().body(erro(SenhaPolitica.MENSAGEM));
        }
        if (dto.getPerfil() == null) {
            return ResponseEntity.badRequest().body(erro("O perfil é obrigatório."));
        }
        String acessoMFV = normalizarAcessoMFV(dto.getAcessoMFV());
        if (acessoMFV == null) {
            return ResponseEntity.badRequest().body(erro("Acesso MFV deve ser Sim ou Não."));
        }

        String perfil = dto.getPerfil().trim().toLowerCase(Locale.ROOT);
        if (!perfil.equals("aluno") && !perfil.equals("professor") && !perfil.equals("admin")) {
            return ResponseEntity.badRequest().body(erro("Perfil inválido."));
        }
        if (perfil.equals("aluno") && (dto.getRa() == null || dto.getRa().trim().isEmpty())) {
            return ResponseEntity.badRequest().body(erro("RA é obrigatório para aluno."));
        }
        if (perfil.equals("aluno") && !dto.getRa().trim().matches("\\d{11}")) {
            return ResponseEntity.badRequest().body(erro("O RA deve ter exatamente 11 números."));
        }
        if (perfil.equals("aluno") && repo.existsByRa(dto.getRa().trim())) {
            return ResponseEntity.badRequest().body(erro("Já existe um aluno com esse RA."));
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
        u.setAcessoMFV(acessoMFV);
        if (perfil.equals("aluno")) {
            u.setRa(dto.getRa().trim());
        } else {
            u.setRa(null);
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
        if (dto.getNome().trim().length() > 50) {
            return ResponseEntity.badRequest().body(erro("O nome deve ter no máximo 50 caracteres."));
        }

        String email = dto.getEmail().trim();
        String perfil = dto.getPerfil().trim().toLowerCase(Locale.ROOT);
        String acessoMFV = dto.getAcessoMFV() == null ? null : normalizarAcessoMFV(dto.getAcessoMFV());
        if (dto.getAcessoMFV() != null && acessoMFV == null) {
            return ResponseEntity.badRequest().body(erro("Acesso MFV deve ser Sim ou Não."));
        }
        if (!perfil.equals("aluno") && !perfil.equals("professor") && !perfil.equals("admin")) {
            return ResponseEntity.badRequest().body(erro("Perfil inválido."));
        }
        if (perfil.equals("aluno") && (dto.getRa() == null || dto.getRa().trim().isEmpty())) {
            return ResponseEntity.badRequest().body(erro("RA é obrigatório para aluno."));
        }
        if (perfil.equals("aluno") && !dto.getRa().trim().matches("\\d{11}")) {
            return ResponseEntity.badRequest().body(erro("O RA deve ter exatamente 11 números."));
        }
        if (perfil.equals("aluno") && repo.existsByRaAndIdNot(dto.getRa().trim(), id)) {
            return ResponseEntity.badRequest().body(erro("Já existe um aluno com esse RA."));
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
        if (acessoMFV != null) {
            usuario.setAcessoMFV(acessoMFV);
        }
        usuario.setRa(perfil.equals("aluno") ? dto.getRa().trim() : null);
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            if (!SenhaPolitica.valida(dto.getSenha())) {
                return ResponseEntity.badRequest().body(erro(SenhaPolitica.MENSAGEM));
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
            u.setRa(null);
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

    private String normalizarAcessoMFV(String valor) {
        if (valor == null || valor.isBlank()) {
            return "N";
        }
        String normalizado = valor.trim().toUpperCase(Locale.ROOT);
        return normalizado.equals("S") || normalizado.equals("N") ? normalizado : null;
    }
}
