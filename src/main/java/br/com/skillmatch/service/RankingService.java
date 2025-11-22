package br.com.skillmatch.service;
import br.com.skillmatch.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RankingService {

    @Inject
    EntityManager em;

    public List<Ranking> getRankingMensal(String mes) {
        return Ranking.list("mesReferencia = ?1 ORDER BY posicao ASC", mes);
    }

    public Ranking getPosicaoRankingUsuario(Long usuarioId) {
        Ranking ranking = Ranking.find("usuario.id = ?1 ORDER BY mesReferencia DESC", usuarioId).firstResult();
        if (ranking == null) {
            throw new RuntimeException("Ranking não encontrado para este usuário");
        }
        return ranking;
    }

    @Transactional
    public Ranking adicionarAoRanking(Long usuarioId, String mesReferencia) {
        Usuario usuario = Usuario.findById(usuarioId);
        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        if (Ranking.count("usuario.id = ?1 and mesReferencia = ?2", usuarioId, mesReferencia) > 0) {
            throw new RuntimeException("Usuário já está no ranking para este mês");
        }

        Long totalRankings = Ranking.count("mesReferencia = ?1", mesReferencia);
        int novaPosicao = totalRankings.intValue() + 1;

        Ranking novoRanking = new Ranking();
        novoRanking.usuario = usuario;
        novoRanking.mesReferencia = mesReferencia;
        novoRanking.posicao = novaPosicao;
        novoRanking.pontuacao = 0L;

        Ranking.persist(novoRanking);
        return novoRanking;
    }

    @Transactional
    public Map<String, Object> adicionarTodosAoRanking(String mesReferencia) {
        List<UsuarioCarreira> usuariosComCarreira = UsuarioCarreira.listAll();

        if (usuariosComCarreira.isEmpty()) {
            throw new RuntimeException("Nenhum usuário com carreira encontrado");
        }

        int usuariosAdicionados = 0;
        int usuariosJaExistentes = 0;
        int erros = 0;
        List<Map<String, Object>> resultados = new ArrayList<>();

        Long totalRankingsAtual = Ranking.count("mesReferencia = ?1", mesReferencia);
        int proximaPosicao = totalRankingsAtual.intValue() + 1;

        for (UsuarioCarreira usuarioCarreira : usuariosComCarreira) {
            try {
                Long usuarioId = usuarioCarreira.usuario.id;

                if (Ranking.count("usuario.id = ?1 and mesReferencia = ?2", usuarioId, mesReferencia) > 0) {
                    usuariosJaExistentes++;
                    resultados.add(Map.of(
                            "usuarioId", usuarioId,
                            "usuarioNome", usuarioCarreira.usuario.nome,
                            "status", "JÁ_EXISTE",
                            "mensagem", "Usuário já está no ranking para este mês"));
                    continue;
                }

                Ranking novoRanking = new Ranking();
                novoRanking.usuario = usuarioCarreira.usuario;
                novoRanking.mesReferencia = mesReferencia;
                novoRanking.posicao = proximaPosicao;
                novoRanking.pontuacao = 0L;

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

        Map<String, Object> resumo = Map.of(
                "mesReferencia", mesReferencia,
                "totalUsuariosProcessados", usuariosComCarreira.size(),
                "usuariosAdicionados", usuariosAdicionados,
                "usuariosJaExistentes", usuariosJaExistentes,
                "erros", erros,
                "resultadosDetalhados", resultados);

        return resumo;
    }

    @Transactional
    public Map<String, Object> atualizarRankingIndividual(Long usuarioId, String mesReferencia) {
        Usuario usuario = Usuario.findById(usuarioId);
        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        if (!Ranking.isMesMaisRecente(mesReferencia)) {
            Ranking ranking = Ranking.find(
                    "usuario.id = ?1 AND mesReferencia = ?2",
                    usuarioId, mesReferencia).firstResult();

            return Map.of(
                    "mensagem", "Ranking não atualizado - não é o mês mais recente",
                    "mesReferencia", mesReferencia,
                    "pontuacaoAtual", ranking != null ? ranking.pontuacao : 0,
                    "posicaoAtual", ranking != null ? ranking.posicao : 0,
                    "atualizado", false);
        }

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
            throw new RuntimeException("Ranking não encontrado para este usuário e mês");
        }

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

        Ranking rankingAtualizado = Ranking.find(
                "usuario.id = ?1 AND mesReferencia = ?2",
                usuarioId, mesReferencia).firstResult();

        em.flush();

        return Map.of(
                "mensagem", "Ranking atualizado e posições reordenadas com sucesso",
                "mesReferencia", mesReferencia,
                "usuarioId", usuarioId,
                "usuarioNome", usuario.nome,
                "novaPontuacao", rankingAtualizado.pontuacao,
                "posicaoAtual", rankingAtualizado.posicao,
                "pontuacaoAtualizada", pontuacaoAtualizada,
                "posicoesReordenadas", posicoesAtualizadas,
                "atualizado", true);
    }

    @Transactional
    public Map<String, Object> atualizarTodosRankingsMesAtual() {
        YearMonth mesAtual = YearMonth.now();
        String mesReferencia = mesAtual.format(DateTimeFormatter.ofPattern("yyyy-MM"));

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

        em.flush();

        return Map.of(
                "mensagem", "Atualização em lote concluída com consultas nativas",
                "mesReferencia", mesReferencia,
                "pontuacoesAtualizadas", pontuacoesAtualizadas,
                "posicoesReordenadas", posicoesAtualizadas);
    }
}