package br.com.aulas.service;

import br.com.aulas.exception.RegistroNaoEncontradoException;
import br.com.aulas.model.SolicitacaoExclusao;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.SolicitacaoExclusaoRepository;
import br.com.aulas.repository.UsuarioRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExclusaoContaService {

    public static final int PRAZO_DIAS = 15;
    public static final String PENDENTE = "Pendente";
    public static final String CONCLUIDA = "Concluida";
    public static final String CANCELADA = "Cancelada";

    private final SolicitacaoExclusaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final AuditoriaService auditoriaService;

    public ExclusaoContaService(SolicitacaoExclusaoRepository solicitacaoRepository, UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate,
                                AuditoriaService auditoriaService) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void solicitar(Usuario usuario, String senhaAtual) {
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            auditoriaService.registrar("LGPD", "EXCLUSAO_SOLICITACAO_FALHA",
                    "Usuário " + usuario.getId() + " informou a senha atual incorreta", false);
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }
        if (solicitacaoRepository.findByIdUsuarioAndStatus(usuario.getId(), PENDENTE).isPresent()) {
            throw new IllegalArgumentException("Já existe uma solicitação de exclusão em andamento.");
        }

        LocalDateTime agora = LocalDateTime.now();
        SolicitacaoExclusao solicitacao = new SolicitacaoExclusao();
        solicitacao.setIdUsuario(usuario.getId());
        solicitacao.setEmail(usuario.getEmail());
        solicitacao.setStatus(PENDENTE);
        solicitacao.setIp(auditoriaService.ipRequisicao());
        solicitacao.setDtSolicitacao(agora);
        solicitacao.setDtLimite(agora.plusDays(PRAZO_DIAS));
        solicitacaoRepository.save(solicitacao);

        auditoriaService.registrar("LGPD", "EXCLUSAO_SOLICITADA",
                "Usuário " + usuario.getId() + " solicitou a exclusão da conta, prazo até " + solicitacao.getDtLimite().toLocalDate(), true);
    }

    @Transactional
    public void cancelar(Usuario usuario) {
        SolicitacaoExclusao solicitacao = solicitacaoRepository.findByIdUsuarioAndStatus(usuario.getId(), PENDENTE)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Não há solicitação de exclusão em andamento."));

        solicitacao.setStatus(CANCELADA);
        solicitacao.setDtConclusao(LocalDateTime.now());
        solicitacaoRepository.save(solicitacao);

        auditoriaService.registrar("LGPD", "EXCLUSAO_CANCELADA",
                "Usuário " + usuario.getId() + " cancelou a solicitação de exclusão da conta", true);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoExclusao> listarVencidas() {
        return solicitacaoRepository.findByStatusAndDtLimiteLessThanEqual(PENDENTE, LocalDateTime.now());
    }

    @Transactional
    public void concluir(SolicitacaoExclusao solicitacao) {
        Usuario usuario = usuarioRepository.findById(solicitacao.getIdUsuario())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado."));
        String email = usuario.getEmail();

        jdbcTemplate.queryForObject("SELECT set_config('app.usuario', ?, true)", String.class, "Sistema LGPD");
        jdbcTemplate.update("DELETE FROM usuariosturmas WHERE idusuario = ?", usuario.getId());
        jdbcTemplate.update("DELETE FROM equipesmembros WHERE idusuario = ?", usuario.getId());
        jdbcTemplate.update("DELETE FROM aulasaluno WHERE idusuario = ?", usuario.getId());
        jdbcTemplate.update("DELETE FROM simuladosrealizacoes WHERE idusuario = ?", usuario.getId());
        jdbcTemplate.update("DELETE FROM logcodigoverificacao WHERE LOWER(email) = LOWER(?)", email);

        usuario.setNome("Usuário removido");
        usuario.setEmail("removido-" + usuario.getId() + "@anonimizado.invalid");
        usuario.setRgm(null);
        usuario.setSenha(null);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        solicitacao.setEmail(email);
        solicitacao.setStatus(CONCLUIDA);
        solicitacao.setDtConclusao(LocalDateTime.now());
        solicitacaoRepository.save(solicitacao);

        auditoriaService.registrar(email, "LGPD", "CONTA_ANONIMIZADA",
                "Usuário " + usuario.getId() + " anonimizado; mantidos registros de auditoria, acessos, aceites do termo e a solicitação", true);
    }
}
