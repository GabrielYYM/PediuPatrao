package com.umc.pediupatrao.dto;

import java.math.BigDecimal;

public record VendaPeriodoDTO(
        String data, 
        BigDecimal total, 
        long quantidade) {
}

