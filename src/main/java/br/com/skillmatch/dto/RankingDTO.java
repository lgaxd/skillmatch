package br.com.skillmatch.dto;

public class RankingDTO {
    private String mesReferencia;

    public RankingDTO() {
        // Construtor padrão necessário para deserialização JSON
    }

    public RankingDTO(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public String getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }
}