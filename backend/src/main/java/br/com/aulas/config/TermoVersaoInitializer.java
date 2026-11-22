package br.com.aulas.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.aulas.repository.TermoVersaoRepository;

@Configuration
public class TermoVersaoInitializer {

    @Bean
    CommandLineRunner validarVersaoTermo(TermoVersaoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                throw new IllegalStateException("Cadastre ao menos uma versão do termo na tabela TermosVersoes "
                        + "(database/11-melhorias-admin.sql).");
            }
        };
    }
}
