package br.com.skillmatch.dto;

import java.time.LocalDate;

public class RegisterDTO {
    public String nome;
    public String email;
    public String senha;
    public LocalDate dataNascimento;

    public RegisterDTO(String nome, String email, String senha, LocalDate dataNascimento) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.dataNascimento = dataNascimento;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

}
