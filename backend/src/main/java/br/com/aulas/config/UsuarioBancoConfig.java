package br.com.aulas.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Configuration
public class UsuarioBancoConfig {

    @Bean
    static BeanPostProcessor usuarioBancoDataSource() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof DataSource dataSource && !(bean instanceof UsuarioDataSource)) {
                    return new UsuarioDataSource(dataSource);
                }
                return bean;
            }
        };
    }

    static class UsuarioDataSource extends DelegatingDataSource {

        UsuarioDataSource(DataSource dataSource) {
            super(dataSource);
        }

        @Override
        public Connection getConnection() throws SQLException {
            return identificar(super.getConnection());
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return identificar(super.getConnection(username, password));
        }

        private Connection identificar(Connection connection) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement("SELECT set_config('app.usuario', ?, false)")) {
                statement.setString(1, usuarioLogado());
                statement.execute();
            } catch (SQLException exception) {
                connection.close();
                throw exception;
            }
            return connection;
        }

        private String usuarioLogado() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                return "";
            }
            return authentication.getName();
        }
    }
}
