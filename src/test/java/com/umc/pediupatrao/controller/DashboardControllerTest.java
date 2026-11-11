package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.config.SecurityConfig;
import com.umc.pediupatrao.dto.DashboardResumoDTO;
import com.umc.pediupatrao.dto.PedidoStatusDTO;
import com.umc.pediupatrao.dto.ProdutoRankingDTO;
import com.umc.pediupatrao.dto.VendaPeriodoDTO;
import com.umc.pediupatrao.service.DashboardService;
import com.umc.pediupatrao.service.UsuarioDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    @DisplayName("ADMIN deve conseguir consultar o resumo de KPIs com sucesso (HTTP 200)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testObterResumoComAdmin() throws Exception {
        DashboardResumoDTO resumo = new DashboardResumoDTO(
                new BigDecimal("1500.00"), 20, 2, new BigDecimal("75.00"));
        when(dashboardService.obterResumo(any(), any())).thenReturn(resumo);

        mockMvc.perform(get("/api/dashboard/resumo")
                        .param("inicio", "2026-11-01")
                        .param("fim", "2026-11-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faturamento").value(1500.00))
                .andExpect(jsonPath("$.pedidosFinalizados").value(20))
                .andExpect(jsonPath("$.pedidosCancelados").value(2))
                .andExpect(jsonPath("$.ticketMedio").value(75.00));
    }

    @Test
    @DisplayName("GERENTE deve conseguir consultar as vendas temporais com sucesso (HTTP 200)")
    @WithMockUser(username = "gerente", roles = {"GERENTE"})
    void testObterVendasComGerente() throws Exception {
        List<VendaPeriodoDTO> vendas = List.of(
                new VendaPeriodoDTO("2026-11-10", new BigDecimal("500.00"), 5)
        );
        when(dashboardService.obterVendasPorPeriodo(any(), any())).thenReturn(vendas);

        mockMvc.perform(get("/api/dashboard/vendas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].data").value("2026-11-10"))
                .andExpect(jsonPath("$[0].total").value(500.00))
                .andExpect(jsonPath("$[0].quantidade").value(5));
    }

    @Test
    @DisplayName("ADMIN deve conseguir consultar a distribuição de status (HTTP 200)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testObterStatusComAdmin() throws Exception {
        List<PedidoStatusDTO> statusList = List.of(
                new PedidoStatusDTO("FINALIZADO", 10),
                new PedidoStatusDTO("CANCELADO", 2)
        );
        when(dashboardService.obterDistribuicaoStatus(any(), any())).thenReturn(statusList);

        mockMvc.perform(get("/api/dashboard/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("FINALIZADO"))
                .andExpect(jsonPath("$[0].quantidade").value(10));
    }

    @Test
    @DisplayName("ADMIN deve conseguir consultar o ranking de produtos (HTTP 200)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testObterRankingProdutosComAdmin() throws Exception {
        List<ProdutoRankingDTO> ranking = List.of(
                new ProdutoRankingDTO("prod-1", "Pizza Calabresa", 15)
        );
        when(dashboardService.obterRankingProdutos(any(), any(), any(Integer.class))).thenReturn(ranking);

        mockMvc.perform(get("/api/dashboard/ranking-produtos")
                        .param("limite", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produtoNome").value("Pizza Calabresa"))
                .andExpect(jsonPath("$[0].quantidadeVendida").value(15));
    }

    @Test
    @DisplayName("Usuário sem permissão (ex: CLIENTE) deve receber HTTP 403 Forbidden")
    @WithMockUser(username = "cliente", roles = {"CLIENTE"})
    void testAcessoNegadoParaUsuarioComum() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Acesso anônimo deve ser barrado")
    void testAcessoAnonimo() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection()); 
    }
}

