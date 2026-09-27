package br.com.aulas.exception;

public class AulaNotFoundException extends RuntimeException {

    public AulaNotFoundException(Integer id) {
        super("Aula não encontrada: " + id);
    }
}