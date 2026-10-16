package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemsPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    public Pedido criarPedido(Pedido pedido) {
        pedido.setDataHoraEntrada(LocalDateTime.now());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuarioLogado = (auth != null) ? auth.getName() : "SISTEMA";
        pedido.setResponsavelRegistro(usuarioLogado);

        double subtotalGeral = 0.0;
        if (pedido.getItens() != null) {
            for (ItemsPedido item : pedido.getItens()) {
                Produto produto = produtoRepository.findById(item.getProdutoId())
                        .orElseThrow(
                                () -> new RuntimeException("Produto não encontrado" + item.getProdutoId()));

                item.setNome(produto.getNome());
                item.setPrecoUnidade(produto.getPreco());

                double totalItem = produto.getPreco() * item.getQuantidade();
                item.setTotal(totalItem);

                subtotalGeral += totalItem;
            }
        }

        Double desconto = pedido.getDesconto();
        if (desconto != null && desconto > 0) {
            boolean podeAplicarDesconto = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE") || a.getAuthority().equals("ROLE_ADMIN"));

            if (!podeAplicarDesconto) {
                throw new AccessDeniedException("Sem perções para descontos");
            }
        } else {
            desconto = 0.0;
            pedido.setDesconto(desconto);
        }

        pedido.setValorTotal(Math.max(0.0, subtotalGeral - desconto));

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                "INCLUSÃO DE PEDIDO",
                "PEDIDO",
                pedidoSalvo.getId(),
                "valorTotal",
                null,
                String.format("%.2f", pedidoSalvo.getValorTotal()),
                null
        );

        return pedidoSalvo;
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido atualizarStatus(String id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado " + id));

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalStateException("Não é permitido alterar o status de um pedido já cancelado.");
        }

        if (novoStatus == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException("Para cancelar o pedido, utilize a operação de cancelamento informando a justificativa.");
        }

        StatusPedido atual = pedido.getStatus();
        boolean transicaoValida = false;

        if (atual == StatusPedido.RECEBIDO && novoStatus == StatusPedido.EM_PREPARACAO) {
            transicaoValida = true;
        } else if (atual == StatusPedido.EM_PREPARACAO && novoStatus == StatusPedido.PRONTO) {
            transicaoValida = true;
        } else if (atual == StatusPedido.PRONTO && novoStatus == StatusPedido.RETIRADO) {
            transicaoValida = true;
        } else if (atual == StatusPedido.RETIRADO && novoStatus == StatusPedido.FINALIZADO) {
            transicaoValida = true;
        }

        if (!transicaoValida) {
            throw new IllegalStateException("Transição de status inválida de " + atual + " para " + novoStatus);
        }

        if (novoStatus == StatusPedido.RETIRADO) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String usuarioLogado = (auth != null) ? auth.getName() : "SISTEMA";
            pedido.setDataHoraSaida(LocalDateTime.now());
            pedido.setResponsavelSaida(usuarioLogado);
        }

        pedido.setStatus(novoStatus);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                "MUDANÇA DE STATUS",
                "PEDIDO",
                pedidoSalvo.getId(),
                "status",
                atual.name(),
                novoStatus.name(),
                null
        );

        return pedidoSalvo;
    }

    public Pedido cancelarPedido(String id, String justificativa) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado" + id));

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalStateException("O pedido já está cancelado");
        }

        if (pedido.getStatus() == StatusPedido.RETIRADO || pedido.getStatus() == StatusPedido.FINALIZADO) {
            throw new IllegalStateException("O pedido já foi retirados ou finalizados");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean podeCancelar = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!podeCancelar) {
            throw new AccessDeniedException("Apenas gerentes têm permissão para cancelamento");
        }

        if (justificativa == null || justificativa.trim().isEmpty()) {
            throw new IllegalArgumentException("Justifique o cancelamento");
        }

        StatusPedido statusAnterior = pedido.getStatus();
        String usuarioLogado = (auth != null) ? auth.getName() : "SISTEMA";

        pedido.setStatus(StatusPedido.CANCELADO);
        pedido.setJustificativaCancelamento(justificativa.trim());
        pedido.setDataHoraCancelamento(LocalDateTime.now());
        pedido.setResponsavelCancelamento(usuarioLogado);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                "CANCELAMENTO",
                "PEDIDO",
                pedidoSalvo.getId(),
                "status",
                statusAnterior.name(),
                "CANCELADO",
                justificativa.trim()
        );

        return pedidoSalvo;
    }

    public Pedido aplicarDesconto(String id, Double percentual) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + id));

        if (pedido.getStatus() == StatusPedido.RETIRADO || pedido.getStatus() == StatusPedido.FINALIZADO || pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalStateException("O pedido já foi retirados ou finalizados");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean podeDesconto = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!podeDesconto) {
            throw new AccessDeniedException("Apenas Gerentes têm permissão para aplicar descontos");
        }

        if (percentual == null || percentual <= 0 || percentual > 20.0) {
            throw new IllegalArgumentException("Desconto maximo de 20%.");
        }

        Double descontoAnterior = pedido.getDesconto() != null ? pedido.getDesconto() : 0.0;

        double subtotal = 0.0;
        if (pedido.getItens() != null) {
            subtotal = pedido.getItens().stream().mapToDouble(ItemsPedido::getTotal).sum();
        }

        double valorDesconto = subtotal * (percentual / 100.0);
        pedido.setDesconto(valorDesconto);
        pedido.setValorTotal(Math.max(0.0, subtotal - valorDesconto));

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Auditoria: Desconto
        auditoriaService.registrar(
                "APLICAR DESCONTO",
                "PEDIDO",
                pedidoSalvo.getId(),
                "desconto",
                String.format("%.2f", descontoAnterior),
                String.format("%.2f (%.1f%%)", valorDesconto, percentual),
                null
        );

        return pedidoSalvo;
    }
}
