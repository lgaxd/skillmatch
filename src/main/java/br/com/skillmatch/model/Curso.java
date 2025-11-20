package br.com.skillmatch.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "TB_CURSO")
public class Curso extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    public Long id;

    @ManyToOne
    @JoinColumn(name = "id_skill")
    public Skill skill;

    @Column(name = "nome_curso")
    public String nome;

    @Column(name = "link_curso")
    public String link;
}