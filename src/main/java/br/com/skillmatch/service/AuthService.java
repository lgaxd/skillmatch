package br.com.skillmatch.service;

import br.com.skillmatch.dto.*;
import br.com.skillmatch.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

    public UsuarioResponseDTO login(LoginDTO loginDto) {
        LoginUsuario loginUsuario = LoginUsuario.find("email", loginDto.getEmail()).firstResult();

        if (loginUsuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        if (!loginUsuario.senha.equals(loginDto.getSenha())) {
            throw new RuntimeException("Senha inválida");
        }

        return new UsuarioResponseDTO(
                loginUsuario.usuario.id,
                loginUsuario.usuario.nome,
                loginUsuario.email);
    }

    @Transactional
    public Usuario register(RegisterDTO dto) {
        if (LoginUsuario.find("email", dto.getEmail()).count() > 0) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.nome = dto.getNome();
        usuario.dataNascimento = dto.getDataNascimento();
        usuario.persist();

        LoginUsuario login = new LoginUsuario();
        login.usuario = usuario;
        login.email = dto.getEmail();
        login.senha = dto.getSenha();
        login.persist();

        return usuario;
    }
}