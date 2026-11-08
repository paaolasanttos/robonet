package br.com.aulas.service;

import br.com.aulas.model.SolicitacaoExclusao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExclusaoContaAgendador {
    private static final Logger logger = LoggerFactory.getLogger(ExclusaoContaAgendador.class);

    private final ExclusaoContaService service;

    public ExclusaoContaAgendador(ExclusaoContaService service) {
        this.service = service;
    }

    @Scheduled(initialDelayString = "${app.exclusao.intervalo-ms}", fixedDelayString = "${app.exclusao.intervalo-ms}")
    public void concluirVencidas() {
        for (SolicitacaoExclusao solicitacao : service.listarVencidas()) {
            try {
                service.concluir(solicitacao);
            } catch (RuntimeException exception) {
                logger.error("Não foi possível concluir a solicitação de exclusão {}.", solicitacao.getId(), exception);
            }
        }
    }
}
