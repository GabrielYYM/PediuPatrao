package com.umc.pediupatrao.entity;

import jakarta.persistence.Entity;

@Entity
public class ItemsPedido {
    private String produtoId;
    private String nome;
    private int quantidade;
    private Double precoUnidade;
    private double total;

    public String getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(String produtoId) {
        this.produtoId = produtoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public Double getPrecoUnidade() {
        return precoUnidade;
    }

    public void setPrecoUnidade(Double precoUnidade) {
        this.precoUnidade = precoUnidade;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

}