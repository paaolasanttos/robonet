package br.com.aulas.service;

import br.com.aulas.dto.LogAlteracaoResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LogAlteracaoService {

    private final JdbcTemplate jdbcTemplate;

    public LogAlteracaoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<LogAlteracaoResponse> listar() {
        return jdbcTemplate.query(
                "SELECT idlogalteracao, tabela, idregistro, operacao, campo, valoranterior, valornovo, usuario, dtalteracao "
                        + "FROM logalteracoes ORDER BY dtalteracao DESC, idlogalteracao DESC LIMIT 500",
                (result, rowNumber) -> new LogAlteracaoResponse(
                        result.getLong("idlogalteracao"),
                        result.getString("tabela"),
                        result.getObject("idregistro") == null ? null : result.getLong("idregistro"),
                        result.getString("operacao"),
                        result.getString("campo"),
                        result.getString("valoranterior"),
                        result.getString("valornovo"),
                        result.getString("usuario"),
                        result.getTimestamp("dtalteracao").toLocalDateTime())
        );
    }
}
