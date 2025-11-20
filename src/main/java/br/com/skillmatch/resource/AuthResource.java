package br.com.skillmatch.resource;

import br.com.skillmatch.dto.*;
import br.com.skillmatch.model.*;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @POST
    @Path("/login")
    public Response login(LoginDTO loginDto) {
        // Busca usuário pelo email na tabela de Login
        LoginUsuario loginUsuario = LoginUsuario.find("email", loginDto.getEmail()).firstResult();

        if (loginUsuario == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Usuário não encontrado").build();
        }

        // ATENÇÃO: Para projeto acadêmico, comparação simples.
        // Em produção, use BCrypt.checkpw(dto.senha, entity.senha)
        if (!loginUsuario.senha.equals(loginDto.getSenha())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Senha inválida").build();
        }

        return Response.ok(new UsuarioResponseDTO(
                loginUsuario.usuario.id,
                loginUsuario.usuario.nome,
                loginUsuario.email)).build();
    }

    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterDTO dto) {
        if (LoginUsuario.find("email", dto.getEmail()).count() > 0) {
            return Response.status(400).entity("Email já cadastrado").build();
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

        return Response.status(Response.Status.CREATED).entity(usuario).build();
    }
}