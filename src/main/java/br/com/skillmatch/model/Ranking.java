package br.com.skillmatch.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "TB_RANKING")
public class Ranking extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ranking")
    public Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    public Usuario usuario;

    @Column(name = "pontuacao_total")
    public Long pontuacao;

    @Column(name = "posicao")
    public Integer posicao;

    @Column(name = "mes_referencia")
    public String mesReferencia;

    // Método para persistir
    public static void persist(Ranking ranking) {
        PanacheEntityBase.persist(ranking);
    }

    // NOVO MÉTODO: Verificar se é o mês mais recente
    public static boolean isMesMaisRecente(String mesReferencia) {
        // Formato esperado: "2024-01"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth mesAtual = YearMonth.now();
        YearMonth mesParam = YearMonth.parse(mesReferencia, formatter);
        
        return mesParam.equals(mesAtual);
    }

    // NOVO MÉTODO: Buscar XP total dos meses anteriores
    public static Long getXpMesesAnteriores(Long usuarioId, String mesReferencia) {
        List<Ranking> rankingsAnteriores = Ranking.list(
            "usuario.id = ?1 AND mesReferencia < ?2 ORDER BY mesReferencia", 
            usuarioId, mesReferencia
        );
        
        return rankingsAnteriores.stream()
            .mapToLong(r -> r.pontuacao != null ? r.pontuacao : 0L)
            .sum();
    }
}