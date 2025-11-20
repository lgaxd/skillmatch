package br.com.skillmatch.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "TB_RANKING")
public class Ranking extends PanacheEntityBase {
    @Id
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
}