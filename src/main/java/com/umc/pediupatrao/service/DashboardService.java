package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.DashboardResumoDTO;
import com.umc.pediupatrao.dto.PedidoStatusDTO;
import com.umc.pediupatrao.dto.ProdutoRankingDTO;
import com.umc.pediupatrao.dto.VendaPeriodoDTO;
import com.umc.pediupatrao.entity.ItemsPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private PedidoRepository pedidoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DashboardResumoDTO obterResumo(LocalDate inicio, LocalDate fim) {
        List<Pedido> pedidos = buscarPedidosPorPeriodo(inicio, fim);

        long pedidosFinalizados = pedidos.stream()
                .filter(p -> p.getStatus() == StatusPedido.FINALIZADO)
                .count();

        long pedidosCancelados = pedidos.stream()
                .filter(p -> p.getStatus() == StatusPedido.CANCELADO)
                .count();

        BigDecimal faturamento = pedidos.stream()
                .filter(p -> p.getStatus() == StatusPedido.FINALIZADO && p.getValorTotal() != null)
                .map(p -> BigDecimal.valueOf(p.getValorTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal ticketMedio = pedidosFinalizados > 0
                ? faturamento.divide(BigDecimal.valueOf(pedidosFinalizados), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return new DashboardResumoDTO(faturamento, pedidosFinalizados, pedidosCancelados, ticketMedio);
    }

    public List<VendaPeriodoDTO> obterVendasPorPeriodo(LocalDate inicio, LocalDate fim) {
        List<Pedido> pedidos = buscarPedidosPorPeriodo(inicio, fim);

        Map<String, List<Pedido>> pedidosPorData = pedidos.stream()
                .filter(p -> p.getStatus() != StatusPedido.CANCELADO && p.getDataHoraEntrada() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getDataHoraEntrada().toLocalDate().format(DATE_FORMATTER),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<VendaPeriodoDTO> resultado = new ArrayList<>();
        pedidosPorData.forEach((data, lista) -> {
            BigDecimal totalDia = lista.stream()
                    .filter(p -> p.getValorTotal() != null)
                    .map(p -> BigDecimal.valueOf(p.getValorTotal()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            resultado.add(new VendaPeriodoDTO(data, totalDia, lista.size()));
        });

        return resultado;
    }

    public List<PedidoStatusDTO> obterDistribuicaoStatus(LocalDate inicio, LocalDate fim) {
        List<Pedido> pedidos = buscarPedidosPorPeriodo(inicio, fim);

        Map<StatusPedido, Long> contagemPorStatus = pedidos.stream()
                .filter(p -> p.getStatus() != null)
                .collect(Collectors.groupingBy(Pedido::getStatus, Collectors.counting()));

        List<PedidoStatusDTO> resultado = new ArrayList<>();
        for (StatusPedido status : StatusPedido.values()) {
            long qtd = contagemPorStatus.getOrDefault(status, 0L);
            resultado.add(new PedidoStatusDTO(status.name(), qtd));
        }

        return resultado;
    }

    public List<ProdutoRankingDTO> obterRankingProdutos(LocalDate inicio, LocalDate fim, int limite) {
        List<Pedido> pedidos = buscarPedidosPorPeriodo(inicio, fim);

        Map<String, ProdutoRankingAccumulator> produtosAgrupados = new HashMap<>();

        for (Pedido pedido : pedidos) {
            if (pedido.getStatus() != StatusPedido.CANCELADO && pedido.getItens() != null) {
                for (ItemsPedido item : pedido.getItens()) {
                    String id = item.getProdutoId() != null ? item.getProdutoId() : item.getNome();
                    produtosAgrupados
                            .computeIfAbsent(id, k -> new ProdutoRankingAccumulator(id, item.getNome()))
                            .adicionarQuantidade(item.getQuantidade());
                }
            }
        }

        return produtosAgrupados.values().stream()
                .sorted(Comparator.comparingLong(ProdutoRankingAccumulator::getQuantidade).reversed())
                .limit(limite > 0 ? limite : 5)
                .map(acc -> new ProdutoRankingDTO(acc.getId(), acc.getNome(), acc.getQuantidade()))
                .collect(Collectors.toList());
    }

    private List<Pedido> buscarPedidosPorPeriodo(LocalDate inicio, LocalDate fim) {
        LocalDate dataIni = inicio != null ? inicio : LocalDate.now().minusDays(30);
        LocalDate dataFim = fim != null ? fim : LocalDate.now();

        if (dataIni.isAfter(dataFim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data inicial não pode ser posterior à data final.");
        }

        if (ChronoUnit.DAYS.between(dataIni, dataFim) > 366) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O período da consulta não pode exceder 1 ano (366 dias).");
        }

        LocalDateTime inicioDateTime = dataIni.atStartOfDay();
        LocalDateTime fimDateTime = dataFim.atTime(23, 59, 59, 999999999);

        return pedidoRepository.findByDataHoraEntradaBetween(inicioDateTime, fimDateTime);
    }

    private static class ProdutoRankingAccumulator {
        private final String id;
        private final String nome;
        private long quantidade;

        public ProdutoRankingAccumulator(String id, String nome) {
            this.id = id;
            this.nome = nome;
            this.quantidade = 0;
        }

        public void adicionarQuantidade(int qtd) {
            this.quantidade += qtd;
        }

        public String getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public long getQuantidade() {
            return quantidade;
        }
    }
}
