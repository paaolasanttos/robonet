package br.com.aulas.controller;

import br.com.aulas.dto.AuthResponse;
import br.com.aulas.dto.RequestCodeRequest;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.UsuarioRepository;
import br.com.aulas.security.JwtService;
import br.com.aulas.security.LoginCodeService;
import br.com.aulas.service.AuditoriaService;
import br.com.aulas.service.GmailService;
import br.com.aulas.service.TermoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void professorWithMfvDisabledReceivesTokenWithoutLoginCode() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        LoginCodeService loginCodeService = mock(LoginCodeService.class);
        GmailService gmailService = mock(GmailService.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        AuditoriaService auditoriaService = mock(AuditoriaService.class);
        TermoService termoService = mock(TermoService.class);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("professor@umc.br");
        usuario.setPerfil("Professor");
        usuario.setAtivo(true);
        usuario.setSenha("encoded");
        usuario.setAcessoMFV("N");

        when(usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha", "encoded")).thenReturn(true);
        when(jwtService.generate(usuario.getEmail(), "Professor", false)).thenReturn("token-professor");
        when(termoService.aceitouVersaoAtual(usuario)).thenReturn(false);

        AuthController controller = new AuthController(
                usuarioRepository, passwordEncoder, jwtService, loginCodeService, gmailService,
                jdbcTemplate, auditoriaService, termoService, "robonet.umc@gmail.com"
        );
        RequestCodeRequest request = new RequestCodeRequest();
        request.setEmail(usuario.getEmail());
        request.setSenha("senha");

        ResponseEntity<?> response = controller.requestCode(request);

        assertEquals(200, response.getStatusCode().value());
        AuthResponse auth = assertInstanceOf(AuthResponse.class, response.getBody());
        assertEquals("token-professor", auth.token());
        assertEquals("Professor", auth.usuario().perfil());
        verify(loginCodeService, never()).issue(anyString());
        verify(gmailService, never()).sendLoginCode(anyString(), anyString(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void configuredAdministratorWithMfvEnabledReceivesLoginCode() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        LoginCodeService loginCodeService = mock(LoginCodeService.class);
        GmailService gmailService = mock(GmailService.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        AuditoriaService auditoriaService = mock(AuditoriaService.class);
        TermoService termoService = mock(TermoService.class);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("robonet.umc@gmail.com");
        usuario.setPerfil("admin");
        usuario.setAtivo(true);
        usuario.setSenha("encoded");
        usuario.setAcessoMFV("S");

        when(usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha", "encoded")).thenReturn(true);
        when(loginCodeService.issue(usuario.getEmail())).thenReturn("123456");

        AuthController controller = new AuthController(
                usuarioRepository, passwordEncoder, jwtService, loginCodeService, gmailService,
                jdbcTemplate, auditoriaService, termoService, " ROBONET.UMC@GMAIL.COM "
        );
        RequestCodeRequest request = new RequestCodeRequest();
        request.setEmail(usuario.getEmail());
        request.setSenha("senha");

        ResponseEntity<?> response = controller.requestCode(request);

        assertEquals(200, response.getStatusCode().value());
        verify(loginCodeService).issue(usuario.getEmail());
        verify(gmailService).sendLoginCode(usuario.getEmail(), "123456", loginCodeService.expirationSeconds());
        verify(jwtService, never()).generate(anyString(), anyString(), org.mockito.ArgumentMatchers.anyBoolean());
    }
}
