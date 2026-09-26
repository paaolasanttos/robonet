package br.com.aulas.dto;

public record AuthResponse(String token, UsuarioResumo usuario) {
    public record UsuarioResumo(Long id, String nome, String email, String perfil) {}
}
