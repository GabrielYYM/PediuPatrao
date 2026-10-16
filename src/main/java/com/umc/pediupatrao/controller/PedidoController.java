package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('GERENTE', 'ATENDENTE')")
    public Pedido criarPedido(@RequestBody Pedido pedido) {
        return pedidoService.criarPedido(pedido);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public List<Pedido> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('GERENTE', 'ATENDENTE')")
    public Pedido atualizarStatus(@PathVariable String id, @RequestParam StatusPedido status) {
        return pedidoService.atualizarStatus(id, status);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido cancelarPedido(@PathVariable String id, @RequestParam String justificativa) {
        return pedidoService.cancelarPedido(id, justificativa);
    }

    @PutMapping("/{id}/desconto")
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido aplicarDesconto(@PathVariable String id, @RequestParam Double percentual) {
        return pedidoService.aplicarDesconto(id, percentual);
    }

}
