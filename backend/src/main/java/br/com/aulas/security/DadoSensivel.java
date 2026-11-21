package br.com.aulas.security;

public final class DadoSensivel {

    private DadoSensivel() {
    }

    public static String mascararEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        String valor = email.trim();
        int arroba = valor.indexOf('@');
        if (arroba <= 0) {
            return valor.charAt(0) + "*****";
        }
        String dominio = valor.substring(arroba + 1);
        int ponto = dominio.lastIndexOf('.');
        String sufixo = ponto >= 0 ? dominio.substring(ponto) : "";
        String inicioDominio = dominio.isEmpty() ? "" : dominio.substring(0, 1);
        return valor.charAt(0) + "*****@" + inicioDominio + "*****" + sufixo;
    }

    public static String mascararEmailComPrefixo(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        String valor = email.trim();
        int caracteresVisiveis = Math.min(5, valor.length());
        return valor.substring(0, caracteresVisiveis) + "*".repeat(valor.length() - caracteresVisiveis);
    }
}
