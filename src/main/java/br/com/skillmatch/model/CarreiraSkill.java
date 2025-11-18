package br.com.skillmatch.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "TB_CARREIRA_SKILL")
public class CarreiraSkill extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carreira_skill")
    public Long id;

    @ManyToOne
    @JoinColumn(name = "id_carreira")
    public Carreira carreira;

    @ManyToOne
    @JoinColumn(name = "id_skill")
    public Skill skill;

    @Column(name = "ordem_trilha")
    public Integer ordem;
}