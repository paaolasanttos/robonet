package br.com.aulas.security;

public final class SenhaPolitica {
    public static final String REGEX = "^(?=.*\\p{Lu})(?=.*\\p{Ll})(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).{8,}$";
    public static final String MENSAGEM = "A senha deve ter pelo menos 8 caracteres, uma letra maiúscula, uma minúscula, um número e um caractere especial.";

    private SenhaPolitica() {
    }

    public static boolean valida(String senha) {
        return senha != null && senha.matches(REGEX);
    }
}
