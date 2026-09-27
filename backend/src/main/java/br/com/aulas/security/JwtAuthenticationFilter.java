package br.com.aulas.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                String role = claims.get("role", String.class);
                String rawRole = role == null ? "" : role.trim();
                String normalizedRole = rawRole.startsWith("ROLE_") ? rawRole.substring(5) : rawRole;
                String finalRole = switch (normalizedRole.toLowerCase(Locale.ROOT)) {
                    case "administrador", "admin" -> "ADMIN";
                    case "professor" -> "PROFESSOR";
                    case "aluno" -> "ALUNO";
                    default -> normalizedRole.isBlank() ? "" : normalizedRole.toUpperCase(Locale.ROOT);
                };

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (!finalRole.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + finalRole));
                }
                if (Boolean.TRUE.equals(claims.get("termo", Boolean.class))) {
                    authorities.add(new SimpleGrantedAuthority("TERMO_ACEITO"));
                }
                if (Boolean.TRUE.equals(claims.get("termo", Boolean.class)) && !finalRole.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + finalRole));
                }
                var authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
