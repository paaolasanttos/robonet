package br.com.aulas.repository;

import br.com.aulas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmailIgnoreCaseAndAtivoTrue(String email);
    java.util.Optional<Usuario> findByEmailIgnoreCaseAndAtivoTrue(String email);
    boolean existsByRa(String ra);
    boolean existsByRaAndIdNot(String ra, Long id);
}
