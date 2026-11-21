package br.com.aulas.service;

import br.com.aulas.dto.SolicitacaoCadastroRequest;
import br.com.aulas.dto.SolicitacaoCadastroResponse;
import br.com.aulas.exception.RegistroNaoEncontradoException;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.DadoSensivel;
import br.com.aulas.security.SenhaPolitica;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class SolicitacaoCadastroService {
    public static final String PENDENTE = "Pendente";
    public static final String APROVADA = "Aprovada";
    public static final String RECUSADA = "Recusada";

    private static final String EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private static final String SELECT_SQL =
            "SELECT idsolicitacaocadastro, nome, email, ra, perfil, status, motivo, dtsolicitacao, dtanalise, usranalise "
                    + "FROM solicitacoescadastro ";

    private final JdbcTemplate jdbcTemplate;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TurmaService turmaService;
    private final AuditoriaService auditoriaService;

    public SolicitacaoCadastroService(JdbcTemplate jdbcTemplate, UsuarioRepository usuarioRepository,
                                      PasswordEncoder passwordEncoder, TurmaService turmaService,
                                      AuditoriaService auditoriaService) {
        this.jdbcTemplate = jdbcTemplate;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.turmaService = turmaService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public void solicitar(SolicitacaoCadastroRequest dados) {
        String nome = texto(dados.nome());
        String email = texto(dados.email()).toLowerCase(Locale.ROOT);
        String perfil = texto(dados.perfil()).toLowerCase(Locale.ROOT);
        String ra = texto(dados.ra());

        if (nome.isEmpty()) {
            throw new IllegalArgumentException("Informe o nome.");
        }
        if (nome.length() > 50) {
            throw new IllegalArgumentException("O nome deve ter no máximo 50 caracteres.");
        }
        if (email.isEmpty() || email.length() > 255 || !email.matches(EMAIL)) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        if (!perfil.equals("aluno") && !perfil.equals("professor")) {
            throw new IllegalArgumentException("Selecione o perfil aluno ou professor.");
        }
        if (perfil.equals("aluno") && !ra.matches("\\d{11}")) {
            throw new IllegalArgumentException("O RA deve ter exatamente 11 números.");
        }
        if (!SenhaPolitica.valida(dados.senha())) {
            throw new IllegalArgumentException(SenhaPolitica.MENSAGEM);
        }
        if (usuarioRepository.existsByEmailIgnoreCaseAndAtivoTrue(email)) {
            throw new IllegalArgumentException("Já existe um usuário com esse e-mail. Use a opção Esqueci minha senha.");
        }
        if (existePendente("LOWER(email) = LOWER(?)", email)) {
            throw new IllegalArgumentException("Já existe uma solicitação de cadastro em análise para esse e-mail.");
        }
        if (perfil.equals("aluno") && (usuarioRepository.existsByRa(ra) || existePendente("ra = ?", ra))) {
            throw new IllegalArgumentException("Já existe um cadastro ou solicitação com esse RA.");
        }

        Long id = jdbcTemplate.queryForObject(
                "INSERT INTO solicitacoescadastro (nome, email, ra, perfil, senha, status, ip, dtsolicitacao) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING idsolicitacaocadastro",
                Long.class,
                nome, email, perfil.equals("aluno") ? ra : null, perfil, passwordEncoder.encode(dados.senha()),
                PENDENTE, auditoriaService.ipRequisicao()
        );

        auditoriaService.registrar(null, "USUARIOS", "CADASTRO_SOLICITADO",
                "Solicitação de cadastro " + id + " registrada para análise do administrador", true);
        auditoriaService.registrar(null, "CRIPTOGRAFIA", "SENHA_CRIPTOGRAFADA",
                "Senha da solicitação de cadastro " + id + " salva com hash BCrypt", true);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoCadastroResponse> listar() {
        return jdbcTemplate.query(
                SELECT_SQL + "ORDER BY CASE WHEN status = ? THEN 0 ELSE 1 END, dtsolicitacao DESC",
                (result, rowNumber) -> resumo(result),
                PENDENTE
        );
    }

    @Transactional
    public SolicitacaoCadastroResponse detalhar(Long id) {
        SolicitacaoCadastroResponse solicitacao = buscar(id);
        auditoriaService.registrar("LGPD", "DADO_SENSIVEL_VISUALIZADO",
                "Dados da solicitação de cadastro " + id + " visualizados para análise", true);
        return solicitacao;
    }

    @Transactional
    public void aprovar(Long id, Long turmaId, String emailAnalista) {
        SolicitacaoCadastroResponse solicitacao = buscarPendente(id);
        if (usuarioRepository.existsByEmailIgnoreCaseAndAtivoTrue(solicitacao.email())) {
            throw new IllegalArgumentException("Já existe um usuário ativo com esse e-mail.");
        }
        if (solicitacao.ra() != null && usuarioRepository.existsByRa(solicitacao.ra())) {
            throw new IllegalArgumentException("Já existe um aluno com esse RA.");
        }
        if (turmaId != null) {
            turmaService.validarTurma(turmaId);
        }

        String senha = jdbcTemplate.queryForObject(
                "SELECT senha FROM solicitacoescadastro WHERE idsolicitacaocadastro = ?", String.class, id);

        Usuario usuario = new Usuario();
        usuario.setNome(solicitacao.nome());
        usuario.setEmail(solicitacao.email());
        usuario.setSenha(senha);
        usuario.setPerfil(solicitacao.perfil());
        usuario.setRa(solicitacao.ra());
        usuario.setAcessoMFV("N");
        usuario.setAtivo(true);
        Usuario salvo = usuarioRepository.saveAndFlush(usuario);
        if (turmaId != null) {
            turmaService.salvarTurmasUsuario(salvo.getId(), List.of(turmaId));
        }

        jdbcTemplate.update(
                "UPDATE solicitacoescadastro SET status = ?, senha = NULL, dtanalise = CURRENT_TIMESTAMP, usranalise = ? "
                        + "WHERE idsolicitacaocadastro = ?",
                APROVADA, emailAnalista, id
        );
        auditoriaService.registrar("USUARIOS", "CADASTRO_APROVADO",
                "Solicitação de cadastro " + id + " aprovada; usuário " + salvo.getId() + " criado com perfil " + salvo.getPerfil(), true);
    }

    @Transactional
    public void recusar(Long id, String motivo, String emailAnalista) {
        String motivoValidado = texto(motivo);
        if (motivoValidado.isEmpty()) {
            throw new IllegalArgumentException("Informe o motivo da recusa.");
        }
        if (motivoValidado.length() > 2000) {
            throw new IllegalArgumentException("O motivo deve ter até 2000 caracteres.");
        }
        buscarPendente(id);
        jdbcTemplate.update(
                "UPDATE solicitacoescadastro SET status = ?, motivo = ?, senha = NULL, dtanalise = CURRENT_TIMESTAMP, "
                        + "usranalise = ? WHERE idsolicitacaocadastro = ?",
                RECUSADA, motivoValidado, emailAnalista, id
        );
        auditoriaService.registrar("USUARIOS", "CADASTRO_RECUSADO", "Solicitação de cadastro " + id + " recusada", true);
    }

    private boolean existePendente(String condicao, String valor) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM solicitacoescadastro WHERE " + condicao + " AND status = ?)",
                Boolean.class, valor, PENDENTE));
    }

    private SolicitacaoCadastroResponse buscarPendente(Long id) {
        jdbcTemplate.queryForList("SELECT idsolicitacaocadastro FROM solicitacoescadastro WHERE idsolicitacaocadastro = ? FOR UPDATE",
                Long.class, id);
        SolicitacaoCadastroResponse solicitacao = buscar(id);
        if (!PENDENTE.equals(solicitacao.status())) {
            throw new IllegalArgumentException("Esta solicitação de cadastro já foi analisada.");
        }
        return solicitacao;
    }

    private SolicitacaoCadastroResponse buscar(Long id) {
        return jdbcTemplate.query(SELECT_SQL + "WHERE idsolicitacaocadastro = ?", (result, rowNumber) -> response(result), id)
                .stream().findFirst()
                .orElseThrow(() -> new RegistroNaoEncontradoException("Solicitação de cadastro não encontrada."));
    }

    private SolicitacaoCadastroResponse response(ResultSet result) throws SQLException {
        return new SolicitacaoCadastroResponse(
                result.getLong("idsolicitacaocadastro"),
                result.getString("nome"),
                result.getString("email"),
                result.getString("ra"),
                result.getString("perfil"),
                result.getString("status"),
                result.getString("motivo"),
                data(result, "dtsolicitacao"),
                data(result, "dtanalise"),
                result.getString("usranalise")
        );
    }

    private SolicitacaoCadastroResponse resumo(ResultSet result) throws SQLException {
        return new SolicitacaoCadastroResponse(
                result.getLong("idsolicitacaocadastro"),
                null,
                DadoSensivel.mascararEmail(result.getString("email")),
                null,
                result.getString("perfil"),
                result.getString("status"),
                result.getString("motivo"),
                data(result, "dtsolicitacao"),
                data(result, "dtanalise"),
                result.getString("usranalise")
        );
    }

    private LocalDateTime data(ResultSet result, String coluna) throws SQLException {
        return result.getTimestamp(coluna) == null ? null : result.getTimestamp(coluna).toLocalDateTime();
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
