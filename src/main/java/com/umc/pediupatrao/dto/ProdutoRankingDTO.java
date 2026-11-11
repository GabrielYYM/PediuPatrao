package com.umc.pediupatrao.dto;

public record ProdutoRankingDTO(
        String produtoId, 
        String produtoNome, 
        long quantidadeVendida) {
}

