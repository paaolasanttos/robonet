package br.com.aulas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;

@Configuration
public class AdminInitializer {
    private static final String LEGACY_ADMIN_EMAIL = "admin@robonet.local";

    @Bean
    CommandLineRunner createInitialAdmin(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${security.admin.email}") String email,
            @Value("${security.admin.password}") String password) {
        return args -> {
            Usuario admin = repository.findByEmailIgnoreCaseAndAtivoTrue(email).orElse(null);
            Usuario legacyAdmin = repository.findByEmailIgnoreCaseAndAtivoTrue(LEGACY_ADMIN_EMAIL).orElse(null);

            if (admin == null && legacyAdmin != null) {
                admin = legacyAdmin;
            }
            if (admin == null) {
                admin = new Usuario();
                admin.setNome("Administrador");
                admin.setPerfil("admin");
                admin.setAtivo(true);
            }

            if (legacyAdmin != null && legacyAdmin.getId() != null && !legacyAdmin.getId().equals(admin.getId())){
                repository.delete(legacyAdmin);
            }

            admin.setEmail(email);
            admin.setPerfil("admin");
            admin.setAtivo(true);

            if (admin.getSenha() == null || admin.getSenha().isBlank()) {
                admin.setSenha(passwordEncoder.encode(password));
            } else if (!passwordEncoder.matches(password, admin.getSenha())) {
                admin.setSenha(passwordEncoder.encode(password));
            }

            repository.save(admin);
        };
    }
}
