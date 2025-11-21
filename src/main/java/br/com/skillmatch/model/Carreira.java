package br.com.skillmatch.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "TB_CARREIRA")
public class Carreira extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carreira")
    public Long id;

    @Column(name = "nome_carreira")
    public String nome;

    @Column(name = "descricao_carreira")
    public String descricao;

    @Column(name = "demanda_mercado", columnDefinition = "NUMBER")
    public Double demanda;
}