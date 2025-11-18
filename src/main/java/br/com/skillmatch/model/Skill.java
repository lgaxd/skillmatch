package br.com.skillmatch.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "TB_SKILL")
public class Skill extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_skill")
    public Long id;

    @Column(name = "nome_skill")
    public String nome;
    
    @Column(name = "nivel_dificuldade")
    public String nivel;
}