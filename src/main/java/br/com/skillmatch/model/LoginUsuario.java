package br.com.skillmatch.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "TB_LOGIN_USUARIO")
public class LoginUsuario extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_login_usuario")
    public Long id;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    public Usuario usuario;

    @Column(name = "email_usuario")
    public String email;

    @Column(name = "senha_usuario")
    public String senha;
}