package br.com.aulas.service;

import br.com.aulas.model.LogAuditoria;
import br.com.aulas.repository.LogAuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {
    private static final Logger logger = LoggerFactory.getLogger(AuditoriaService.class);

    private final LogAuditoriaRepository logAuditoriaRepository;

    public AuditoriaService(LogAuditoriaRepository logAuditoriaRepository) {
        this.logAuditoriaRepository = logAuditoriaRepository;
    }

    public void registrar(String categoria, String acao, String descricao, boolean sucesso) {
        registrar(usuarioLogado(), categoria, acao, descricao, sucesso);
    }

    public void registrar(String email, String categoria, String acao, String descricao, boolean sucesso) {
        try {
            LogAuditoria log = new LogAuditoria();
            log.setEmail(email);
            log.setCategoria(categoria);
            log.setAcao(acao);
            log.setDescricao(descricao);
            log.setIp(ipRequisicao());
            log.setSucesso(sucesso);
            log.setDtRegistro(LocalDateTime.now());
            logAuditoriaRepository.save(log);
        } catch (RuntimeException exception) {
            logger.error("Não foi possível gravar o log de auditoria da ação {}.", acao, exception);
        }
    }

    public List<LogAuditoria> listar() {
        return logAuditoriaRepository.findTop500ByOrderByDtRegistroDesc();
    }

    public String ipRequisicao() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String usuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }
}
