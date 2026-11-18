package br.com.aulas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.aulas.repository.TermoVersaoRepository;

@Configuration
public class TermoVersaoInitializer {

    @Bean
    CommandLineRunner validarVersaoTermo(
            TermoVersaoRepository repository,
            @Value("${app.termo.versao}") String versao) {
        return args -> {
            if (!repository.existsByVersao(versao)) {
                throw new IllegalStateException("Cadastre o texto da versão " + versao
                        + " do termo na tabela TermosVersoes (database/10-termosversoes.sql).");
            }
        };
    }
}
