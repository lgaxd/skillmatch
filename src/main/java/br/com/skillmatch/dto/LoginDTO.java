package br.com.skillmatch.dto;

public class LoginDTO {

    public String email;
    public String senha;

    public LoginDTO(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

}
