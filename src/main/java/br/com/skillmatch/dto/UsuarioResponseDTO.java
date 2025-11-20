package br.com.skillmatch.dto;

public class UsuarioResponseDTO {

    public Long id;
    public String nome;
    public String email;

    public UsuarioResponseDTO(Long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

}
