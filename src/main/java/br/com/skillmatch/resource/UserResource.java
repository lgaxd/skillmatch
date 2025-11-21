package br.com.skillmatch.resource;

import br.com.skillmatch.dto.*;
import br.com.skillmatch.model.*;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    @Path("/{id}")
    public Response getUsuario(@PathParam("id") Long id) {
        Usuario u = Usuario.findById(id);
        return u != null ? Response.ok(u).build() : Response.status(404).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUsuario(@PathParam("id") Long id, Usuario dados) {
        Usuario u = Usuario.findById(id);
        if (u == null)
            return Response.status(404).build();

        u.nome = dados.nome;
        u.dataNascimento = dados.dataNascimento;
        // Panache gerencia o update automaticamente no fim da transação
        return Response.ok(u).build();
    }

    @GET
    @Path("/{id}/carreira-atual")
    public Response getCarreiraAtual(@PathParam("id") Long id) {
        UsuarioCarreira uc = UsuarioCarreira.find("usuario.id = ?1", id).firstResult();
        if (uc == null)
            return Response.status(404).entity("Usuário sem carreira ativa").build();
        return Response.ok(uc).build();
    }

    @POST
    @Path("/{id}/carreira")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response selecionarCarreira(@PathParam("id") Long userId, Carreira carreira) {
        Usuario usuario = Usuario.findById(userId);
        Carreira c = Carreira.findById(carreira.id);

        if (usuario == null || c == null)
            return Response.status(404).build();

        UsuarioCarreira uc = new UsuarioCarreira();
        uc.usuario = usuario;
        uc.carreira = c;
        uc.idStatusJornada = 2L; // 2 = Em Andamento (conforme SQL)
        uc.progresso = 0.0;
        uc.xp = 0L;
        uc.persist();

        return Response.status(201).entity(uc).build();
    }

    @PUT
    @Path("/{id}/carreira/atualizar-progresso")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualizarProgressoCarreira(@PathParam("id") Long userId, ProgressoDTO progressoDto) {
        UsuarioCarreira uc = UsuarioCarreira.find("usuario.id = ?1", userId).firstResult();
        if (uc == null)
            return Response.status(404).entity("Usuário sem carreira ativa").build();

        uc.progresso = progressoDto.getPercentual();
        return Response.ok(uc).build();
    }

    @GET
    @Path("/{id}/cursos")
    public Response getCursosUsuario(@PathParam("id") Long id) {
        return Response.ok(UsuarioCurso.list("usuario.id", id)).build();
    }

    @GET
    @Path("/{id}/ranking")
    public Response getPosicaoRanking(@PathParam("id") Long id) {
        // Exemplo buscando o ranking mais recente (simplificado)
        Ranking r = Ranking.find("usuario.id = ?1 ORDER BY mesReferencia DESC", id).firstResult();
        return r != null ? Response.ok(r).build() : Response.status(404).build();
    }

    @POST
    @Path("/{id}/ranking")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adicionarAoRanking(@PathParam("id") Long id, RankingDTO rankingDto) {
        try {
            Usuario u = Usuario.findById(id);
            if (u == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Usuário não encontrado")
                        .build();
            }

            // Verificar se o usuário já está no ranking para este mês - USANDO COUNT
            Long countExistente = Ranking.count(
                    "usuario.id = ?1 and mesReferencia = ?2",
                    id,
                    rankingDto.getMesReferencia());

            if (countExistente > 0) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Usuário já está no ranking para este mês")
                        .build();
            }

            // Buscar total de rankings do mês usando COUNT (mais eficiente)
            Long totalRankings = Ranking.count("mesReferencia = ?1", rankingDto.getMesReferencia());
            int novaPosicao = totalRankings.intValue() + 1;

            // Criar novo ranking
            Ranking novoRanking = new Ranking();
            novoRanking.usuario = u;
            novoRanking.mesReferencia = rankingDto.getMesReferencia();
            novoRanking.posicao = novaPosicao;
            novoRanking.pontuacao = 0L;

            // Persistir usando o método estático
            Ranking.persist(novoRanking);

            return Response.status(Response.Status.CREATED)
                    .entity(novoRanking)
                    .build();

        } catch (Exception e) {
            e.printStackTrace(); // Para debug
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro ao adicionar usuário ao ranking: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/{id}/xp")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adicionarXp(@PathParam("id") Long id, XpDTO xpDto) {
        // Adiciona XP na carreira atual
        UsuarioCarreira uc = UsuarioCarreira.find("usuario.id", id).firstResult();
        if (uc != null) {
            uc.xp += xpDto.getQuantidadeXp();
        }
        // Aqui você também poderia atualizar a tabela TB_USUARIO_PONTUACAO
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/dashboard")
    public Response getDashboard(@PathParam("id") Long id) {
        Usuario u = Usuario.findById(id);
        if (u == null)
            return Response.status(404).build();

        UsuarioCarreira uc = UsuarioCarreira.find("usuario.id", id).firstResult();
        long cursosConcluidos = UsuarioCurso.count("usuario.id = ?1 AND status = 'Concluído'", id);

        DashboardDTO dashboard = new DashboardDTO(
                u.nome,
                uc != null ? uc.carreira.nome : "Nenhuma",
                uc != null ? uc.progresso : 0.0,
                uc != null ? uc.xp : 0L,
                cursosConcluidos);

        return Response.ok(dashboard).build();
    }

    @GET
    @Path("/{id}/estatisticas")
    public Response getEstatisticas(@PathParam("id") Long id) {
        // Exemplo de estatísticas simples
        Map<String, Object> stats = Map.of(
                "totalCursosIniciados", UsuarioCurso.count("usuario.id", id),
                "totalCursosConcluidos", UsuarioCurso.count("usuario.id = ?1 AND status = 'Concluído'", id));
        return Response.ok(stats).build();
    }
}