package br.com.skillmatch.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "TB_USUARIO_CURSO")
public class UsuarioCurso extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario_curso")
    public Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    public Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_curso")
    public Curso curso;

    @Column(name = "status_curso")
    public String status; // 'Pendente', 'Em andamento', 'Concluído'

    @Column(name = "progresso_percentual")
    public Double progresso;
}