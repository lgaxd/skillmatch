package br.com.skillmatch.resource;

import br.com.skillmatch.dto.*;
import br.com.skillmatch.model.*;
import br.com.skillmatch.service.UserService;
import br.com.skillmatch.service.RankingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @Inject
    RankingService rankingService;

    @GET
    @Path("/{id}")
    public Response getUsuario(@PathParam("id") Long id) {
        try {
            Usuario usuario = userService.getUsuario(id);
            return Response.ok(usuario).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUsuario(@PathParam("id") Long id, Usuario dados) {
        try {
            Usuario usuario = userService.updateUsuario(id, dados);
            return Response.ok(usuario).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}/carreira-atual")
    public Response getCarreiraAtual(@PathParam("id") Long id) {
        try {
            Object carreira = userService.getCarreiraAtual(id);
            return Response.ok(carreira).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{id}/carreira")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response selecionarCarreira(@PathParam("id") Long userId, Carreira carreira) {
        try {
            Object usuarioCarreira = userService.selecionarCarreira(userId, carreira.id);
            return Response.ok(usuarioCarreira).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}/carreira/atualizar-progresso")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualizarProgressoCarreira(@PathParam("id") Long userId, ProgressoDTO progressoDto) {
        try {
            Object usuarioCarreira = userService.atualizarProgressoCarreira(userId, progressoDto.getPercentual());
            return Response.ok(usuarioCarreira).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}/cursos")
    public Response getCursosUsuario(@PathParam("id") Long id) {
        try {
            Object cursos = userService.getCursosUsuario(id);
            return Response.ok(cursos).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}/ranking")
    public Response getPosicaoRanking(@PathParam("id") Long id) {
        try {
            Object ranking = rankingService.getPosicaoRankingUsuario(id);
            return Response.ok(ranking).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{id}/ranking")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adicionarAoRanking(@PathParam("id") Long id, RankingDTO rankingDto) {
        try {
            Object ranking = rankingService.adicionarAoRanking(id, rankingDto.getMesReferencia());
            return Response.status(Response.Status.CREATED).entity(ranking).build();
        } catch (RuntimeException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/ranking/adicionar-todos")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adicionarTodosAoRanking(RankingDTO rankingDto) {
        try {
            Object resultado = rankingService.adicionarTodosAoRanking(rankingDto.getMesReferencia());
            return Response.status(Response.Status.CREATED).entity(resultado).build();
        } catch (RuntimeException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}/ranking/{mes}")
    public Response atualizarRanking(
            @PathParam("id") Long usuarioId,
            @PathParam("mes") String mesReferencia) {
        try {
            Object resultado = rankingService.atualizarRankingIndividual(usuarioId, mesReferencia);
            return Response.ok(resultado).build();
        } catch (RuntimeException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/ranking/atualizar-mes-atual")
    public Response atualizarTodosRankingsMesAtual() {
        try {
            Object resultado = rankingService.atualizarTodosRankingsMesAtual();
            return Response.ok(resultado).build();
        } catch (RuntimeException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{id}/xp")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adicionarXp(@PathParam("id") Long id, XpDTO xpDto) {
        try {
            userService.adicionarXp(id, xpDto.getQuantidadeXp());
            return Response.ok().build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}/dashboard")
    public Response getDashboard(@PathParam("id") Long id) {
        try {
            Object dashboard = userService.getDashboard(id);
            return Response.ok(dashboard).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}/estatisticas")
    public Response getEstatisticas(@PathParam("id") Long id) {
        try {
            Object estatisticas = userService.getEstatisticas(id);
            return Response.ok(estatisticas).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }
}