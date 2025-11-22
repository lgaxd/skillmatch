package br.com.skillmatch.resource;

import br.com.skillmatch.dto.ProgressoDTO;
import br.com.skillmatch.service.CareerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import br.com.skillmatch.service.RankingService;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class CareerResource {

    @Inject
    CareerService careerService;
    @Inject
    RankingService rankingService;

    @GET
    @Path("carreiras")
    public Response listarCarreiras() {
        try {
            Object carreiras = careerService.listarCarreiras();
            return Response.ok(carreiras).build();
        } catch (RuntimeException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("carreiras/{id}")
    public Response detalheCarreira(@PathParam("id") Long id) {
        try {
            Object carreira = careerService.detalheCarreira(id);
            return Response.ok(carreira).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("carreiras/{id}/skills")
    public Response listarSkillsDaCarreira(@PathParam("id") Long idCarreira) {
        try {
            Object skills = careerService.listarSkillsDaCarreira(idCarreira);
            return Response.ok(skills).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("skills/{id}/cursos")
    public Response listarCursosDaSkill(@PathParam("id") Long idSkill) {
        try {
            Object cursos = careerService.listarCursosDaSkill(idSkill);
            return Response.ok(cursos).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("cursos/{id}/iniciar")
    public Response iniciarCurso(@PathParam("id") Long idCurso, @QueryParam("idUsuario") Long idUsuario) {
        try {
            Object usuarioCurso = careerService.iniciarCurso(idCurso, idUsuario);
            return Response.status(201).entity(usuarioCurso).build();
        } catch (RuntimeException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("cursos/{id}/concluir")
    public Response concluirCurso(@PathParam("id") Long idCurso, @QueryParam("idUsuario") Long idUsuario) {
        try {
            Object usuarioCurso = careerService.concluirCurso(idCurso, idUsuario);
            return Response.ok(usuarioCurso).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("cursos/{id}/progresso")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualizarProgressoCurso(@PathParam("id") Long idCurso, @QueryParam("idUsuario") Long idUsuario, ProgressoDTO progresso) {
        try {
            Object usuarioCurso = careerService.atualizarProgressoCurso(idCurso, idUsuario, progresso.getPercentual());
            return Response.ok(usuarioCurso).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("ranking/{mes}")
    public Response getRankingMensal(@PathParam("mes") String mes) {
        try {
            Object ranking = rankingService.getRankingMensal(mes);
            return Response.ok(ranking).build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }
}