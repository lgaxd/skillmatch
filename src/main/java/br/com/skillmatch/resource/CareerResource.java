package br.com.skillmatch.resource;

import br.com.skillmatch.dto.ProgressoDTO;
import br.com.skillmatch.model.*;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CareerResource {

    // --- CARREIRAS ---
    @GET
    @Path("carreiras")
    public Response listarCarreiras() {
        return Response.ok(Carreira.listAll()).build();
    }

    @GET
    @Path("carreiras/{id}")
    public Response detalheCarreira(@PathParam("id") Long id) {
        Carreira c = Carreira.findById(id);
        return c != null ? Response.ok(c).build() : Response.status(404).build();
    }

    @GET
    @Path("carreiras/{id}/skills")
    public Response listarSkillsDaCarreira(@PathParam("id") Long idCarreira) {
        // Retorna a lista de CarreiraSkill (que contem a skill e a ordem)
        return Response.ok(CarreiraSkill.list("carreira.id ORDER BY ordem", idCarreira)).build();
    }

    // --- SKILLS ---
    @GET
    @Path("skills/{id}/cursos")
    public Response listarCursosDaSkill(@PathParam("id") Long idSkill) {
        return Response.ok(Curso.list("skill.id", idSkill)).build();
    }

    // --- CURSOS & PROGRESSO ---
    @POST
    @Path("cursos/{id}/iniciar")
    @Transactional
    public Response iniciarCurso(@PathParam("id") Long idCurso, @QueryParam("idUsuario") Long idUsuario) {
        // Verifica se já existe
        if (UsuarioCurso.count("curso.id = ?1 AND usuario.id = ?2", idCurso, idUsuario) > 0) {
            return Response.status(400).entity("Curso já iniciado").build();
        }

        UsuarioCurso uc = new UsuarioCurso();
        uc.curso = Curso.findById(idCurso);
        uc.usuario = Usuario.findById(idUsuario);
        uc.status = "Em andamento";
        uc.progresso = 0.0;
        uc.persist();
        
        return Response.status(201).entity(uc).build();
    }

    @PUT
    @Path("cursos/{id}/concluir")
    @Transactional
    public Response concluirCurso(@PathParam("id") Long idCurso, @QueryParam("idUsuario") Long idUsuario) {
        UsuarioCurso uc = UsuarioCurso.find("curso.id = ?1 AND usuario.id = ?2", idCurso, idUsuario).firstResult();
        if (uc == null) return Response.status(404).build();
        
        uc.status = "Concluído";
        uc.progresso = 100.0;
        // Poderia adicionar XP aqui
        return Response.ok(uc).build();
    }
    
    @PUT
    @Path("cursos/{id}/progresso")
    @Transactional
    public Response atualizarProgressoCurso(@PathParam("id") Long idCurso, @QueryParam("idUsuario") Long idUsuario, ProgressoDTO progresso) {
        UsuarioCurso uc = UsuarioCurso.find("curso.id = ?1 AND usuario.id = ?2", idCurso, idUsuario).firstResult();
        if (uc == null) return Response.status(404).build();
        
        uc.progresso = progresso.getPercentual();
        if (uc.progresso >= 100.0) {
            uc.status = "Concluído";
        }
        return Response.ok(uc).build();
    }

    // --- RANKING GERAL ---
    @GET
    @Path("ranking/{mes}")
    public Response getRankingMensal(@PathParam("mes") String mes) {
        // Ex: mes = "2024-01"
        return Response.ok(Ranking.list("mesReferencia = ?1 ORDER BY posicao ASC", mes)).build();
    }
}