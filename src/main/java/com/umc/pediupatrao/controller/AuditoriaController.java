package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping
    public List<Auditoria> listarTodas() {
        return auditoriaService.listarTodas();
    }

    @GetMapping("/pedido/{pedidoId}")
    public List<Auditoria> buscarPorPedido(@PathVariable String pedidoId) {
        return auditoriaService.buscarPorEntidade("PEDIDO", pedidoId);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Auditoria> buscarPorCliente(@PathVariable String clienteId) {
        return auditoriaService.buscarPorEntidade("CLIENTE", clienteId);
    }
}
