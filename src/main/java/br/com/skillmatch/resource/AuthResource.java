package br.com.skillmatch.resource;

import br.com.skillmatch.dto.*;
import br.com.skillmatch.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public Response login(LoginDTO loginDto) {
        try {
            UsuarioResponseDTO usuario = authService.login(loginDto);
            return Response.ok(usuario).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/register")
    public Response register(RegisterDTO dto) {
        try {
            Object usuario = authService.register(dto);
            return Response.status(Response.Status.CREATED).entity(usuario).build();
        } catch (RuntimeException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }
}