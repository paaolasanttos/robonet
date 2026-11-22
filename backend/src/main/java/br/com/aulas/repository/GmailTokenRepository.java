package br.com.aulas.repository;

import br.com.aulas.model.GmailToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GmailTokenRepository extends JpaRepository<GmailToken, Long> {
}
