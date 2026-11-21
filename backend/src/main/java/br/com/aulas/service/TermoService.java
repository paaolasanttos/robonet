package br.com.aulas.service;

import br.com.aulas.dto.TermoAtualResponse;
import br.com.aulas.exception.RegistroNaoEncontradoException;
import br.com.aulas.model.TermoAceite;
import br.com.aulas.model.TermoVersao;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.TermoAceiteRepository;
import br.com.aulas.repository.TermoVersaoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class TermoService {

    private static final Pattern VERSAO = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){1,2}");
    private static final Pattern HTML = Pattern.compile("<\\s*/?\\s*[a-zA-Z!]");
    private static final int TAMANHO_MAXIMO = 100000;

    private final TermoAceiteRepository termoAceiteRepository;
    private final TermoVersaoRepository termoVersaoRepository;
    private final AuditoriaService auditoriaService;
    private final JdbcTemplate jdbcTemplate;

    public TermoService(TermoAceiteRepository termoAceiteRepository, TermoVersaoRepository termoVersaoRepository,
                        AuditoriaService auditoriaService, JdbcTemplate jdbcTemplate) {
        this.termoAceiteRepository = termoAceiteRepository;
        this.termoVersaoRepository = termoVersaoRepository;
        this.auditoriaService = auditoriaService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public TermoAtualResponse atual() {
        TermoVersao termo = termoVigente();
        return new TermoAtualResponse(termo.getVersao(), termo.getDtVersao(), termo.getConteudo());
    }

    @Transactional(readOnly = true)
    public List<TermoAtualResponse> versoes() {
        return termoVersaoRepository.findAllByOrderByIdDesc().stream()
                .map(termo -> new TermoAtualResponse(termo.getVersao(), termo.getDtVersao(), termo.getConteudo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public String versaoAtual() {
        return termoVigente().getVersao();
    }

    @Transactional(readOnly = true)
    public boolean aceitouVersaoAtual(Usuario usuario) {
        return termoAceiteRepository.existsByIdUsuarioAndVersao(usuario.getId(), versaoAtual());
    }

    @Transactional(readOnly = true)
    public Set<Long> usuariosQueAceitaramVersaoAtual() {
        return new HashSet<>(jdbcTemplate.queryForList(
                "SELECT DISTINCT idusuario FROM termosaceites WHERE versao = ?", Long.class, versaoAtual()));
    }

    @Transactional
    public void aceitar(Usuario usuario) {
        String versao = versaoAtual();
        if (termoAceiteRepository.existsByIdUsuarioAndVersao(usuario.getId(), versao)) {
            return;
        }

        TermoAceite aceite = new TermoAceite();
        aceite.setIdUsuario(usuario.getId());
        aceite.setVersao(versao);
        aceite.setIp(auditoriaService.ipRequisicao());
        aceite.setDtAceite(LocalDateTime.now());
        termoAceiteRepository.save(aceite);

        auditoriaService.registrar(usuario.getEmail(), "LGPD", "TERMO_ACEITO",
                "Aceite do Termo de Uso e Aviso de Privacidade versão " + versao, true);
    }

    public void recusar(Usuario usuario) {
        auditoriaService.registrar(usuario.getEmail(), "LGPD", "TERMO_RECUSADO",
                "Usuário recusou o Termo de Uso versão " + versaoAtual() + " e teve o acesso bloqueado", true);
    }

    @Transactional
    public TermoAtualResponse publicar(String versao, String conteudo) {
        String novaVersao = versao == null ? "" : versao.trim();
        String texto = conteudo == null ? "" : conteudo.replace("\r\n", "\n").strip();
        if (!VERSAO.matcher(novaVersao).matches()) {
            throw new IllegalArgumentException("Informe a versão no formato 0.4 ou 1.0.");
        }
        if (termoVersaoRepository.existsByVersao(novaVersao)) {
            throw new IllegalArgumentException("Já existe um termo com a versão " + novaVersao + ".");
        }
        if (!versaoMaior(novaVersao, versaoAtual())) {
            throw new IllegalArgumentException("A nova versão deve ser maior que a versão atual " + versaoAtual() + ".");
        }
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Informe o texto do termo.");
        }
        if (texto.length() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("O texto do termo deve ter no máximo " + TAMANHO_MAXIMO + " caracteres.");
        }
        if (HTML.matcher(texto).find()) {
            throw new IllegalArgumentException("O texto do termo não pode conter HTML. Use # para títulos.");
        }

        TermoVersao termo = new TermoVersao();
        termo.setVersao(novaVersao);
        termo.setDtVersao(LocalDate.now());
        termo.setConteudo(texto);
        termoVersaoRepository.save(termo);

        auditoriaService.registrar("LGPD", "TERMO_PUBLICADO",
                "Nova versão " + novaVersao + " do Termo de Uso publicada; todos os usuários deverão aceitar novamente", true);
        return new TermoAtualResponse(termo.getVersao(), termo.getDtVersao(), termo.getConteudo());
    }

    private TermoVersao termoVigente() {
        return termoVersaoRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new RegistroNaoEncontradoException("Nenhuma versão do termo foi cadastrada."));
    }

    private boolean versaoMaior(String nova, String atual) {
        String[] a = nova.split("\\.");
        String[] b = atual.split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? Integer.parseInt(a[i]) : 0;
            int y = i < b.length && b[i].matches("\\d+") ? Integer.parseInt(b[i]) : 0;
            if (x != y) {
                return x > y;
            }
        }
        return false;
    }
}
