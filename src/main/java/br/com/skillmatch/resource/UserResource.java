package br.com.skillmatch.resource;

import br.com.skillmatch.dto.*;
import br.com.skillmatch.model.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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
    @Path("/ranking/adicionar-todos")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adicionarTodosAoRanking(RankingDTO rankingDto) {
        try {
            // Buscar todos os usuários ativos (que possuem carreira)
            List<UsuarioCarreira> usuariosComCarreira = UsuarioCarreira.listAll();

            if (usuariosComCarreira.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Nenhum usuário com carreira encontrado")
                        .build();
            }

            int usuariosAdicionados = 0;
            int usuariosJaExistentes = 0;
            int erros = 0;
            List<Map<String, Object>> resultados = new ArrayList<>();

            // Buscar total atual de rankings do mês para calcular posições iniciais
            Long totalRankingsAtual = Ranking.count("mesReferencia = ?1", rankingDto.getMesReferencia());
            int proximaPosicao = totalRankingsAtual.intValue() + 1;

            for (UsuarioCarreira usuarioCarreira : usuariosComCarreira) {
                try {
                    Long usuarioId = usuarioCarreira.usuario.id;

                    // Verificar se o usuário já está no ranking para este mês
                    Long countExistente = Ranking.count(
                            "usuario.id = ?1 and mesReferencia = ?2",
                            usuarioId,
                            rankingDto.getMesReferencia());

                    if (countExistente > 0) {
                        usuariosJaExistentes++;
                        resultados.add(Map.of(
                                "usuarioId", usuarioId,
                                "usuarioNome", usuarioCarreira.usuario.nome,
                                "status", "JÁ_EXISTE",
                                "mensagem", "Usuário já está no ranking para este mês"));
                        continue;
                    }

                    // Criar novo ranking
                    Ranking novoRanking = new Ranking();
                    novoRanking.usuario = usuarioCarreira.usuario;
                    novoRanking.mesReferencia = rankingDto.getMesReferencia();
                    novoRanking.posicao = proximaPosicao;
                    novoRanking.pontuacao = 0L; // Pontuação inicial zero

                    // Persistir
                    Ranking.persist(novoRanking);

                    usuariosAdicionados++;
                    proximaPosicao++;

                    resultados.add(Map.of(
                            "usuarioId", usuarioId,
                            "usuarioNome", usuarioCarreira.usuario.nome,
                            "status", "ADICIONADO",
                            "posicao", novoRanking.posicao,
                            "mensagem", "Usuário adicionado ao ranking com sucesso"));

                } catch (Exception e) {
                    erros++;
                    resultados.add(Map.of(
                            "usuarioId", usuarioCarreira.usuario.id,
                            "usuarioNome", usuarioCarreira.usuario.nome,
                            "status", "ERRO",
                            "mensagem", "Erro ao adicionar usuário: " + e.getMessage()));
                }
            }

            // Resumo da operação
            Map<String, Object> resumo = Map.of(
                    "mesReferencia", rankingDto.getMesReferencia(),
                    "totalUsuariosProcessados", usuariosComCarreira.size(),
                    "usuariosAdicionados", usuariosAdicionados,
                    "usuariosJaExistentes", usuariosJaExistentes,
                    "erros", erros,
                    "resultadosDetalhados", resultados);

            return Response.status(Response.Status.CREATED)
                    .entity(resumo)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro ao adicionar usuários ao ranking: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}/ranking/{mes}")
    @Transactional
    public Response atualizarRanking(
            @PathParam("id") Long usuarioId,
            @PathParam("mes") String mesReferencia) {

        try {
            // Verificar se usuário existe
            Usuario usuario = Usuario.findById(usuarioId);
            if (usuario == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Usuário não encontrado")
                        .build();
            }

            // Verificar se é o mês mais recente
            if (!Ranking.isMesMaisRecente(mesReferencia)) {
                Ranking ranking = Ranking.find(
                        "usuario.id = ?1 AND mesReferencia = ?2",
                        usuarioId, mesReferencia).firstResult();

                return Response.ok(Map.of(
                        "mensagem", "Ranking não atualizado - não é o mês mais recente",
                        "mesReferencia", mesReferencia,
                        "pontuacaoAtual", ranking != null ? ranking.pontuacao : 0,
                        "posicaoAtual", ranking != null ? ranking.posicao : 0,
                        "atualizado", false)).build();
            }

            EntityManager em = Ranking.getEntityManager();

            // FASE 1: Atualizar pontuação do usuário específico CORRIGIDA
            String updatePontuacao = """
                    UPDATE TB_RANKING
                    SET pontuacao_total = GREATEST(
                        (SELECT NVL(xp_total, 0) FROM (
                            SELECT xp_total FROM TB_USUARIO_CARREIRA
                            WHERE id_usuario = ?1
                            ORDER BY id_usuario_carreira DESC
                        ) WHERE ROWNUM = 1) -
                        (SELECT NVL(SUM(pontuacao_total), 0) FROM TB_RANKING
                         WHERE id_usuario = ?1 AND mes_referencia < ?2),
                        0
                    )
                    WHERE id_usuario = ?1 AND mes_referencia = ?2
                    """;

            int pontuacaoAtualizada = em.createNativeQuery(updatePontuacao)
                    .setParameter(1, usuarioId)
                    .setParameter(2, mesReferencia)
                    .executeUpdate();

            if (pontuacaoAtualizada == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Ranking não encontrado para este usuário e mês")
                        .build();
            }

            // FASE 2: Reordenar todas as posições do mês
            String updatePosicoes = """
                    MERGE INTO TB_RANKING target
                    USING (
                        SELECT
                            id_ranking,
                            ROW_NUMBER() OVER (ORDER BY pontuacao_total DESC) as nova_posicao
                        FROM TB_RANKING
                        WHERE mes_referencia = ?1
                    ) source
                    ON (target.id_ranking = source.id_ranking)
                    WHEN MATCHED THEN UPDATE SET
                        target.posicao = source.nova_posicao
                    """;

            int posicoesAtualizadas = em.createNativeQuery(updatePosicoes)
                    .setParameter(1, mesReferencia)
                    .executeUpdate();

            // Buscar dados atualizados
            Ranking rankingAtualizado = Ranking.find(
                    "usuario.id = ?1 AND mesReferencia = ?2",
                    usuarioId, mesReferencia).firstResult();

            em.flush();

            return Response.ok(Map.of(
                    "mensagem", "Ranking atualizado e posições reordenadas com sucesso",
                    "mesReferencia", mesReferencia,
                    "usuarioId", usuarioId,
                    "usuarioNome", usuario.nome,
                    "novaPontuacao", rankingAtualizado.pontuacao,
                    "posicaoAtual", rankingAtualizado.posicao,
                    "pontuacaoAtualizada", pontuacaoAtualizada,
                    "posicoesReordenadas", posicoesAtualizadas,
                    "atualizado", true)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro ao atualizar ranking: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/ranking/atualizar-mes-atual")
    @Transactional
    public Response atualizarTodosRankingsMesAtual() {
        try {
            // Obter mês atual no formato "yyyy-MM"
            YearMonth mesAtual = YearMonth.now();
            String mesReferencia = mesAtual.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            // FASE 1: Atualizar pontuações usando consulta nativa CORRIGIDA
            EntityManager em = Ranking.getEntityManager();

            String updatePontuacoes = """
                    UPDATE TB_RANKING r
                    SET pontuacao_total = (
                        SELECT GREATEST(NVL((
                            SELECT uc.xp_total
                            FROM TB_USUARIO_CARREIRA uc
                            WHERE uc.id_usuario = r.id_usuario
                            AND ROWNUM = 1
                        ), 0) - NVL((
                            SELECT SUM(r2.pontuacao_total)
                            FROM TB_RANKING r2
                            WHERE r2.id_usuario = r.id_usuario
                            AND r2.mes_referencia < r.mes_referencia
                        ), 0), 0)
                        FROM DUAL
                    )
                    WHERE mes_referencia = ?1
                    """;

            int pontuacoesAtualizadas = em.createNativeQuery(updatePontuacoes)
                    .setParameter(1, mesReferencia)
                    .executeUpdate();

            // FASE 2: Reordenar posições usando consulta nativa com ROW_NUMBER()
            String updatePosicoes = """
                    MERGE INTO TB_RANKING target
                    USING (
                        SELECT
                            id_ranking,
                            ROW_NUMBER() OVER (ORDER BY pontuacao_total DESC) as nova_posicao
                        FROM TB_RANKING
                        WHERE mes_referencia = ?1
                    ) source
                    ON (target.id_ranking = source.id_ranking)
                    WHEN MATCHED THEN UPDATE SET
                        target.posicao = source.nova_posicao
                    """;

            int posicoesAtualizadas = em.createNativeQuery(updatePosicoes)
                    .setParameter(1, mesReferencia)
                    .executeUpdate();

            // Forçar o flush
            em.flush();

            return Response.ok(Map.of(
                    "mensagem", "Atualização em lote concluída com consultas nativas",
                    "mesReferencia", mesReferencia,
                    "pontuacoesAtualizadas", pontuacoesAtualizadas,
                    "posicoesReordenadas", posicoesAtualizadas))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro ao atualizar rankings: " + e.getMessage())
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