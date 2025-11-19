package br.com.skillmatch.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "TB_USUARIO")
public class Usuario extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    public Long id;

    @Column(name = "nome_usuario")
    public String nome;

    @Column(name = "data_nascimento")
    public LocalDate dataNascimento;

    // Helper para dashboard
    public static Usuario findByEmail(String email) {
        LoginUsuario login = LoginUsuario.find("email", email).firstResult();
        return login != null ? login.usuario : null;
    }
}
