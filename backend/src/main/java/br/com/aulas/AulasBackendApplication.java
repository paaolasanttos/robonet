package br.com.aulas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AulasBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AulasBackendApplication.class, args);
    }
}