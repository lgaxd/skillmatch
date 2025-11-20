package br.com.skillmatch.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "TB_USUARIO_CARREIRA")
public class UsuarioCarreira extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario_carreira")
    public Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    public Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_carreira")
    public Carreira carreira;

    // Status fixo: 1-Não Iniciada, 2-Em Andamento, etc.
    @Column(name = "id_status_jornada")
    public Long idStatusJornada;

    @Column(name = "progresso_percentual")
    public Double progresso;

    @Column(name = "xp_total")
    public Long xp;
}