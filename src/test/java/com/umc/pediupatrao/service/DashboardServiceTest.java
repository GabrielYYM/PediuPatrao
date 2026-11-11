package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.DashboardResumoDTO;
import com.umc.pediupatrao.dto.PedidoStatusDTO;
import com.umc.pediupatrao.dto.ProdutoRankingDTO;
import com.umc.pediupatrao.dto.VendaPeriodoDTO;
import com.umc.pediupatrao.entity.ItemsPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private List<Pedido> pedidosMock;

    @BeforeEach
    void setUp() {
        pedidosMock = new ArrayList<>();

        Pedido p1 = new Pedido();
        p1.setStatus(StatusPedido.FINALIZADO);
        p1.setValorTotal(100.0);
        p1.setDataHoraEntrada(LocalDateTime.now().minusDays(2));
        ItemsPedido item1 = new ItemsPedido();
        item1.setProdutoId("prod-1");
        item1.setNome("Pizza Calabresa");
        item1.setQuantidade(2);
        p1.setItens(List.of(item1));
        pedidosMock.add(p1);

        Pedido p2 = new Pedido();
        p2.setStatus(StatusPedido.FINALIZADO);
        p2.setValorTotal(50.0);
        p2.setDataHoraEntrada(LocalDateTime.now().minusDays(1));
        ItemsPedido item2 = new ItemsPedido();
        item2.setProdutoId("prod-2");
        item2.setNome("Refrigerante");
        item2.setQuantidade(3);
        p2.setItens(List.of(item2));
        pedidosMock.add(p2);

        Pedido p3 = new Pedido();
        p3.setStatus(StatusPedido.CANCELADO);
        p3.setValorTotal(80.0);
        p3.setDataHoraEntrada(LocalDateTime.now().minusDays(1));
        pedidosMock.add(p3);

        Pedido p4 = new Pedido();
        p4.setStatus(StatusPedido.EM_PREPARACAO);
        p4.setValorTotal(60.0);
        p4.setDataHoraEntrada(LocalDateTime.now());
        pedidosMock.add(p4);
    }

    @Test
    @DisplayName("Deve calcular corretamente os KPIs de resumo (Faturamento, Finalizados, Cancelados e Ticket Médio)")
    void testObterResumo() {
        when(pedidoRepository.findByDataHoraEntradaBetween(any(), any())).thenReturn(pedidosMock);

        DashboardResumoDTO resumo = dashboardService.obterResumo(LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(resumo);
        
        assertEquals(new BigDecimal("150.00"), resumo.faturamento());
        assertEquals(2, resumo.pedidosFinalizados());
        assertEquals(1, resumo.pedidosCancelados());

        assertEquals(new BigDecimal("75.00"), resumo.ticketMedio());
    }

    @Test
    @DisplayName("Deve retornar ticket médio zero quando não houver pedidos finalizados")
    void testObterResumoSemPedidosFinalizados() {
        when(pedidoRepository.findByDataHoraEntradaBetween(any(), any())).thenReturn(Collections.emptyList());

        DashboardResumoDTO resumo = dashboardService.obterResumo(LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(resumo);
        assertEquals(new BigDecimal("0.00"), resumo.faturamento());
        assertEquals(0, resumo.pedidosFinalizados());
        assertEquals(0, resumo.pedidosCancelados());
        assertEquals(new BigDecimal("0.00"), resumo.ticketMedio());
    }

    @Test
    @DisplayName("Deve agrupar vendas por data para gráfico")
    void testObterVendasPorPeriodo() {
        when(pedidoRepository.findByDataHoraEntradaBetween(any(), any())).thenReturn(pedidosMock);

        List<VendaPeriodoDTO> vendas = dashboardService.obterVendasPorPeriodo(LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(vendas);
        assertFalse(vendas.isEmpty());
        assertTrue(vendas.stream().noneMatch(v -> v.total().compareTo(BigDecimal.ZERO) < 0));
    }

    @Test
    @DisplayName("Deve calcular distribuição de pedidos por status")
    void testObterDistribuicaoStatus() {
        when(pedidoRepository.findByDataHoraEntradaBetween(any(), any())).thenReturn(pedidosMock);

        List<PedidoStatusDTO> statusList = dashboardService.obterDistribuicaoStatus(LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(statusList);
        assertEquals(StatusPedido.values().length, statusList.size());

        PedidoStatusDTO finalizado = statusList.stream()
                .filter(s -> s.status().equals(StatusPedido.FINALIZADO.name()))
                .findFirst().orElseThrow();
        assertEquals(2, finalizado.quantidade());

        PedidoStatusDTO cancelado = statusList.stream()
                .filter(s -> s.status().equals(StatusPedido.CANCELADO.name()))
                .findFirst().orElseThrow();
        assertEquals(1, cancelado.quantidade());
    }

    @Test
    @DisplayName("Deve calcular ranking dos produtos mais vendidos ordenados decrescente")
    void testObterRankingProdutos() {
        when(pedidoRepository.findByDataHoraEntradaBetween(any(), any())).thenReturn(pedidosMock);

        List<ProdutoRankingDTO> ranking = dashboardService.obterRankingProdutos(LocalDate.now().minusDays(7), LocalDate.now(), 5);

        assertNotNull(ranking);
        assertEquals(2, ranking.size());

        assertEquals("Refrigerante", ranking.get(0).produtoNome());
        assertEquals(3, ranking.get(0).quantidadeVendida());

        assertEquals("Pizza Calabresa", ranking.get(1).produtoNome());
        assertEquals(2, ranking.get(1).quantidadeVendida());
    }

    @Test
    @DisplayName("Deve lançar ResponseStatusException se o intervalo de datas exceder 1 ano")
    void testRejeitarPeriodoMaiorQueUmAno() {
        LocalDate inicio = LocalDate.now().minusDays(400);
        LocalDate fim = LocalDate.now();

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> dashboardService.obterResumo(inicio, fim)
        );

        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("1 ano"));
    }

    @Test
    @DisplayName("Deve lançar ResponseStatusException se data inicial for posterior à data final")
    void testRejeitarDataInicialMaiorQueFinal() {
        LocalDate inicio = LocalDate.now();
        LocalDate fim = LocalDate.now().minusDays(5);

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> dashboardService.obterResumo(inicio, fim)
        );

        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("posterior"));
    }
}

