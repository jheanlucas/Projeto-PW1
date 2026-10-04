package br.edu.ifpb.sinan.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "notificacoes")
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String agravo;

    @Column(length = 12)
    private String cid10;

    @Column(nullable = false)
    private LocalDate dataNotificacao;

    private LocalDate dataPrimeirosSintomas;

    @Column(nullable = false, length = 160)
    private String nomePaciente;

    private LocalDate dataNascimento;
    private Integer idadeValor;
    private String idadeUnidade;
    private String sexo;
    private String gestante;

    @Column(length = 160)
    private String nomeMae;

    @Column(nullable = false)
    private Boolean resideBrasil;
    private String ufResidencia;
    private String municipioResidencia;
    private String paisResidencia;

    @Column(length = 1000)
    private String observacoes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAgravo() { return agravo; }
    public void setAgravo(String agravo) { this.agravo = agravo; }
    public String getCid10() { return cid10; }
    public void setCid10(String cid10) { this.cid10 = cid10; }
    public LocalDate getDataNotificacao() { return dataNotificacao; }
    public void setDataNotificacao(LocalDate dataNotificacao) { this.dataNotificacao = dataNotificacao; }
    public LocalDate getDataPrimeirosSintomas() { return dataPrimeirosSintomas; }
    public void setDataPrimeirosSintomas(LocalDate dataPrimeirosSintomas) { this.dataPrimeirosSintomas = dataPrimeirosSintomas; }
    public String getNomePaciente() { return nomePaciente; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public Integer getIdadeValor() { return idadeValor; }
    public void setIdadeValor(Integer idadeValor) { this.idadeValor = idadeValor; }
    public String getIdadeUnidade() { return idadeUnidade; }
    public void setIdadeUnidade(String idadeUnidade) { this.idadeUnidade = idadeUnidade; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public String getGestante() { return gestante; }
    public void setGestante(String gestante) { this.gestante = gestante; }
    public String getNomeMae() { return nomeMae; }
    public void setNomeMae(String nomeMae) { this.nomeMae = nomeMae; }
    public Boolean getResideBrasil() { return resideBrasil; }
    public void setResideBrasil(Boolean resideBrasil) { this.resideBrasil = resideBrasil; }
    public String getUfResidencia() { return ufResidencia; }
    public void setUfResidencia(String ufResidencia) { this.ufResidencia = ufResidencia; }
    public String getMunicipioResidencia() { return municipioResidencia; }
    public void setMunicipioResidencia(String municipioResidencia) { this.municipioResidencia = municipioResidencia; }
    public String getPaisResidencia() { return paisResidencia; }
    public void setPaisResidencia(String paisResidencia) { this.paisResidencia = paisResidencia; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
