package com.umc.pediupatrao.dto;

import java.math.BigDecimal;

public record DashboardResumoDTO(
        BigDecimal faturamento,
        long pedidosFinalizados,
        long pedidosCancelados,
        BigDecimal ticketMedio) {
}

