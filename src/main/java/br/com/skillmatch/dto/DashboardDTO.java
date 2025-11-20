package br.com.skillmatch.dto;

public class DashboardDTO {

    public String nomeUsuario;
    public String carreiraAtual;
    public Double progressoCarreira;
    public Long xpTotal;
    public Long cursosConcluidos;

    public DashboardDTO(String nomeUsuario, String carreiraAtual, Double progressoCarreira, Long xpTotal, Long cursosConcluidos) {
        this.nomeUsuario = nomeUsuario;
        this.carreiraAtual = carreiraAtual;
        this.progressoCarreira = progressoCarreira;
        this.xpTotal = xpTotal;
        this.cursosConcluidos = cursosConcluidos;
    }

}
