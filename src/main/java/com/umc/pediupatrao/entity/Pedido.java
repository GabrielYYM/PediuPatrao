package com.umc.pediupatrao.entity;

import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.umc.pediupatrao.entity.enums.StatusPedido;

@Document(collection = "pedidos")
@Entity
public class Pedido {
    @Id
    private String id;
    private String clienteId;
    private StatusPedido status = StatusPedido.RECEBIDO;
    private List<ItemsPedido> itens = new ArrayList<>();
    private Double desconto;
    private Double valorTotal;
    private String responsavelRegistro;
    private LocalDateTime dataHoraEntrada;
    private LocalDateTime dataHoraSaida;
    private String responsavelSaida;
    private String justificativaCancelamento;
    private LocalDateTime dataHoraCancelamento;
    private String responsavelCancelamento;

    public String getJustificativaCancelamento() {
        return justificativaCancelamento;
    }

    public void setJustificativaCancelamento(String justificativaCancelamento) {
        this.justificativaCancelamento = justificativaCancelamento;
    }

    public LocalDateTime getDataHoraCancelamento() {
        return dataHoraCancelamento;
    }

    public void setDataHoraCancelamento(LocalDateTime dataHoraCancelamento) {
        this.dataHoraCancelamento = dataHoraCancelamento;
    }

    public String getResponsavelCancelamento() {
        return responsavelCancelamento;
    }

    public void setResponsavelCancelamento(String responsavelCancelamento) {
        this.responsavelCancelamento = responsavelCancelamento;
    }

    public LocalDateTime getDataHoraSaida() {
        return dataHoraSaida;
    }

    public void setDataHoraSaida(LocalDateTime dataHoraSaida) {
        this.dataHoraSaida = dataHoraSaida;
    }

    public String getResponsavelSaida() {
        return responsavelSaida;
    }

    public void setResponsavelSaida(String responsavelSaida) {
        this.responsavelSaida = responsavelSaida;
    }

    public List<ItemsPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemsPedido> itens) {
        this.itens = itens;
    }

    public Double getDesconto() {
        return desconto;
    }

    public void setDesconto(Double desconto) {
        this.desconto = desconto;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getResponsavelRegistro() {
        return responsavelRegistro;
    }

    public void setResponsavelRegistro(String responsavelRegistro) {
        this.responsavelRegistro = responsavelRegistro;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public LocalDateTime getDataHoraEntrada() {
        return dataHoraEntrada;
    }

    public void setDataHoraEntrada(LocalDateTime dataHoraEntrada) {
        this.dataHoraEntrada = dataHoraEntrada;
    }
}
