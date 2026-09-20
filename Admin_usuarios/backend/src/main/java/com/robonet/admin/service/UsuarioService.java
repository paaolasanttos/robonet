package com.robonet.admin.service;

import com.robonet.admin.dto.UsuarioRequest;
import com.robonet.admin.dto.UsuarioResponse;
import com.robonet.admin.exception.UsuarioNotFoundException;
import com.robonet.admin.model.Usuario;
import com.robonet.admin.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private static final List<String> PERFIS_VALIDOS = List.of("aluno","professor","admin");
    private static final String PERFIL_ALUNO = "aluno";

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(){
        return repository.findAll().stream().map(this::toResponse).toList();
    }
    @Transactional
    public UsuarioResponse criar (UsuarioRequest dados) {
        validarPerfil(dados.getPerfil());
        if ((ehAluno(dados.getPerfil()) && (dados.getRgm() == null) || dados.getRgm().isBlank())) {
            throw new IllegalArgumentException("RGM' é obrigatório para o perfil 'aluno'.");
        }
        if (repository.existsByEmail(dados.getEmail())) {
            throw new IllegalArgumentException("O email já está em uso.");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dados.getNome());
        usuario.setEmail(dados.getEmail());
        usuario.setPerfil(dados.getPerfil());
        usuario.setRgm(ehAluno(dados.getPerfil()) ? dados.getRgm() : null);
        usuario.setAtivo(true);

        return toResponse(repository.save(usuario));
    }
    @Transactional
    public UsuarioResponse alterarPerfil (Long id , String novoPerfil){
        validarPerfil(novoPerfil);
        Usuario usuario = buscarEntidade(id);

        if (!ehAluno(novoPerfil)){
            usuario.setRgm(null);
        }
        return toResponse(repository.save(usuario));
    }
    @Transactional
    public UsuarioResponse alterarStatus (Long id , Boolean ativo){
        Usuario usuario = buscarEntidade(id);
        usuario.setAtivo(ativo);
        return toResponse(repository.save(usuario));
    }
    @Transactional
    public void excluir(Long id ){
        if (!repository.existsById(id)){
            throw new UsuarioNotFoundException(id);
        }
        repository.deleteById(id);
    }
    private Usuario buscarEntidade(Long id){
        return repository.findById(id).orElseThrow(() -> new UsuarioNotFoundException(id));
    }
    private void validarPerfil(String perfil){
        if (perfil == null || !PERFIS_VALIDOS.contains(perfil.toLowerCase())) {
            throw new IllegalArgumentException("Perfil inválido. Use: aluno, professor ou admin." );
        }
    }
    private boolean ehAluno(String perfil) {
        return PERFIL_ALUNO.equals(perfil);
    }

    private UsuarioResponse toResponse(Usuario u){
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getRgm(),
                u.getPerfil(),
                u.getAtivo(),
                u.getCriadoEm()
        );
    }
}
