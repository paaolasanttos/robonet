package br.com.aulas.service;

import br.com.aulas.model.TermoAceite;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.TermoAceiteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TermoService {

    private final TermoAceiteRepository termoAceiteRepository;
    private final AuditoriaService auditoriaService;
    private final String versaoAtual;

    public TermoService(TermoAceiteRepository termoAceiteRepository, AuditoriaService auditoriaService,
                        @Value("${app.termo.versao}") String versaoAtual) {
        this.termoAceiteRepository = termoAceiteRepository;
        this.auditoriaService = auditoriaService;
        this.versaoAtual = versaoAtual;
    }

    @Transactional(readOnly = true)
    public boolean aceitouVersaoAtual(Usuario usuario) {
        return termoAceiteRepository.existsByIdUsuarioAndVersao(usuario.getId(), versaoAtual);
    }

    @Transactional
    public void aceitar(Usuario usuario) {
        if (aceitouVersaoAtual(usuario)) {
            return;
        }

        TermoAceite aceite = new TermoAceite();
        aceite.setIdUsuario(usuario.getId());
        aceite.setVersao(versaoAtual);
        aceite.setIp(auditoriaService.ipRequisicao());
        aceite.setDtAceite(LocalDateTime.now());
        termoAceiteRepository.save(aceite);

        auditoriaService.registrar(usuario.getEmail(), "LGPD", "TERMO_ACEITO",
                "Aceite do Termo de Uso e Aviso de Privacidade versão " + versaoAtual, true);
    }

    public void recusar(Usuario usuario) {
        auditoriaService.registrar(usuario.getEmail(), "LGPD", "TERMO_RECUSADO",
                "Usuário recusou o Termo de Uso versão " + versaoAtual + " e teve o acesso bloqueado", true);
    }
}
