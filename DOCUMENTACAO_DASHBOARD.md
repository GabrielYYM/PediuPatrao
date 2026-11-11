# Documentação Técnica — Dashboard Administrativo (Pediu Patrão)

Este documento atende aos requisitos de entrega do projeto integrador da disciplina de Análise e Projeto de Software / Desenvolvimento de APIs.

---

## 1. Requisitos do Sistema

### 1.1. Requisitos Funcionais (RF)
* **RF01 — Indicadores Gerais de Vendas (KPIs):** Exibição em cards no topo da página de Faturamento Total Líquido (apenas pedidos finalizados), Total de Pedidos Concluídos, Total de Cancelamentos e Ticket Médio (`Faturamento / Concluídos`).
* **RF02 — Análise de Vendas (Gráficos):** Gráfico de linha/área temporal exibindo o histórico diário de receita gerada.
* **RF03 — Situação dos Pedidos:** Gráfico de rosca (*doughnut*) com a distribuição quantitativa de pedidos por status (`RECEBIDO`, `EM_PREPARACAO`, `PRONTO`, `RETIRADO`, `FINALIZADO`, `CANCELADO`).
* **RF04 — Produtos Mais Vendidos:** Ranking com os itens mais vendidos do cardápio e volume total de saídas.
* **RF05 — Filtros de Consulta:** Filtragem dinâmica por período via query parameters (`inicio` e `fim` no formato `AAAA-MM-DD`), com atalhos rápidos (*Hoje*, *7 Dias*, *30 Dias*) e fallback automático para os últimos 30 dias.
* **RF06 — Integração com a API:** Comunicação assíncrona entre front-end e os endpoints REST via requisições `fetch()` em paralelo com `Promise.all()`.
* **RF07 — Atualização e Tratamento de Dados:** Precisão monetária estrita com `java.math.BigDecimal`, proteção contra divisão por zero, isolamento contábil de cancelamentos e validação estrita de intervalo máximo de 1 ano (366 dias).

### 1.2. Requisitos Não Funcionais (RNF)
* **RNF01 — Segurança (RBAC):** Proteção dos endpoints e da interface administrativa no nível de método com Spring Security (`@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")`). Acessos não autorizados recebem `HTTP 403 Forbidden`.
* **RNF02 — Performance e Otimização:** Consultas temporais indexadas diretamente no MongoDB (`findByDataHoraEntradaBetween`), evitando o carregamento desnecessário de dados em memória.
* **RNF03 — Integridade Numérica:** Eliminação de imprecisões de ponto flutuante (`Double`) com o uso de `BigDecimal` e arredondamento `RoundingMode.HALF_UP`.
* **RNF04 — Confiabilidade:** 100% dos fluxos analíticos cobertos por suíte de testes unitários e de integração com MockMvc.
* **RNF05 — Arquitetura Limpa:** Desacoplamento estrito entre Entidades, Repositórios, Serviços, Controladores REST e DTOs baseados em Java 21 `record`.

---

## 2. Documentação dos Endpoints REST

* **URL Base:** `/api/dashboard`
* **Autenticação:** Obrigatória (Perfis: `ADMIN` ou `GERENTE`)
* **Formato de Dados:** JSON (`application/json`)

### Parâmetros Gerais de Consulta (Query Params)
| Parâmetro | Tipo | Obrigatório | Padrão | Descrição |
| :--- | :---: | :---: | :--- | :--- |
| `inicio` | `LocalDate` | Não | 30 dias atrás | Data inicial da consulta no formato `AAAA-MM-DD` |
| `fim` | `LocalDate` | Não | Data atual | Data final da consulta no formato `AAAA-MM-DD` |

---

### Endpoint 1: Indicadores Gerais (KPIs)
* **Rota:** `GET /api/dashboard/resumo`
* **Exemplo de Requisição:** `GET /api/dashboard/resumo?inicio=2026-11-01&fim=2026-11-10`
* **Exemplo de Resposta (HTTP 200 OK):**
```json
{
  "faturamento": 3480.50,
  "pedidosFinalizados": 42,
  "pedidosCancelados": 3,
  "ticketMedio": 82.87
}
```

---

### Endpoint 2: Análise Temporal de Vendas
* **Rota:** `GET /api/dashboard/vendas`
* **Exemplo de Requisição:** `GET /api/dashboard/vendas?inicio=2026-11-01&fim=2026-11-03`
* **Exemplo de Resposta (HTTP 200 OK):**
```json
[
  {
    "data": "2026-11-01",
    "total": 1150.00,
    "quantidade": 14
  },
  {
    "data": "2026-11-02",
    "total": 980.50,
    "quantidade": 12
  },
  {
    "data": "2026-11-03",
    "total": 1350.00,
    "quantidade": 16
  }
]
```

---

### Endpoint 3: Situação dos Pedidos
* **Rota:** `GET /api/dashboard/status`
* **Exemplo de Requisição:** `GET /api/dashboard/status`
* **Exemplo de Resposta (HTTP 200 OK):**
```json
[
  { "status": "RECEBIDO", "quantidade": 2 },
  { "status": "EM_PREPARACAO", "quantidade": 4 },
  { "status": "PRONTO", "quantidade": 1 },
  { "status": "RETIRADO", "quantidade": 0 },
  { "status": "FINALIZADO", "quantidade": 42 },
  { "status": "CANCELADO", "quantidade": 3 }
]
```

---

### Endpoint 4: Ranking de Produtos Mais Vendidos
* **Rota:** `GET /api/dashboard/ranking-produtos`
* **Parâmetro Adicional:** `limite` (inteiro, padrão: `5`)
* **Exemplo de Requisição:** `GET /api/dashboard/ranking-produtos?limite=3`
* **Exemplo de Resposta (HTTP 200 OK):**
```json
[
  {
    "produtoId": "prod-01",
    "produtoNome": "Pizza Calabresa Especial",
    "quantidadeVendida": 28
  },
  {
    "produtoId": "prod-02",
    "produtoNome": "Pizza Quatro Queijos",
    "quantidadeVendida": 22
  },
  {
    "produtoId": "prod-07",
    "produtoNome": "Refrigerante Coca-Cola 2L",
    "quantidadeVendida": 19
  }
]
```

---

### Possíveis Erros da API
| Código HTTP | Causa | Exemplo de Retorno JSON |
| :---: | :--- | :--- |
| **`400 Bad Request`** | Data inicial maior que a final | `{"status": 400, "message": "A data inicial não pode ser posterior à data final."}` |
| **`400 Bad Request`** | Intervalo maior que 1 ano (366 dias) | `{"status": 400, "message": "O período da consulta não pode exceder 1 ano (366 dias)."}` |
| **`403 Forbidden`** | Usuário sem perfil `ADMIN` ou `GERENTE` | `{"status": 403, "error": "Forbidden"}` |
| **`401 Unauthorized`** | Requisição sem autenticação | Redirecionamento para tela de login (`/login`) |

---

## 3. Evidências de Testes Realizados

A suíte de testes automatizados conta com **13 testes executados com 100% de sucesso**:

```text
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.umc.pediupatrao.controller.DashboardControllerTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in com.umc.pediupatrao.controller.DashboardControllerTest
Running com.umc.pediupatrao.service.DashboardServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 -- in com.umc.pediupatrao.service.DashboardServiceTest

Results:
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Detalhamento dos Cenários de Teste:
1. `testObterResumo`: Valida soma exata de receita, exclusão de cancelados e cálculo de ticket médio.
2. `testObterResumoSemPedidosFinalizados`: Valida proteção contra divisão por zero retornando `R$ 0,00`.
3. `testObterVendasPorPeriodo`: Valida agrupamento cronológico e exclusão de cancelados no gráfico de linhas.
4. `testObterDistribuicaoStatus`: Valida contagem correta para todos os status do enum.
5. `testObterRankingProdutos`: Valida ordenação decrescente por mais vendido e aplicação do limite.
6. `testRejeitarPeriodoMaiorQueUmAno`: Valida emissão de `HTTP 400` para intervalos maiores que 366 dias.
7. `testRejeitarDataInicialMaiorQueFinal`: Valida emissão de `HTTP 400` para datas invertidas.
8. `testObterResumoComAdmin`: Valida resposta `HTTP 200` e estrutura JSON para perfil `ADMIN`.
9. `testObterVendasComGerente`: Valida resposta `HTTP 200` para perfil `GERENTE`.
10. `testObterStatusComAdmin`: Valida resposta `HTTP 200` do endpoint de status.
11. `testObterRankingProdutosComAdmin`: Valida resposta `HTTP 200` do ranking com limite.
12. `testAcessoNegadoParaUsuarioComum`: Valida bloqueio `HTTP 403 Forbidden` para perfis sem permissão.
13. `testAcessoAnonimo`: Valida bloqueio/redirecionamento de acessos não autenticados.

---

## 4. Como Acessar o Dashboard Funcional

1. Inicie a aplicação no VS Code ou terminal (`.\mvnw.cmd spring-boot:run`).
2. Acesse: `http://localhost:8080/dashboard`
3. Credenciais para testes (geradas automaticamente via mock no `DataInitializer`):
   * **Administrador:** Usuário `admin` | Senha `teste123`
   * **Gerente:** Usuário `gerente` | Senha `teste123`

---

## 5. Roteiro Sugerido para a Apresentação em Vídeo (Até 3 Minutos)

* **0:00 - 0:30 (Contexto e Arquitetura):**
  > *"Olá! Apresento o Dashboard Administrativo do Pediu Patrão. Desenvolvemos uma arquitetura com Spring Boot 3, Java 21 Records, MongoDB e Chart.js, seguindo boas práticas de desacoplamento REST e segurança baseada em papéis (RBAC)."*
* **0:30 - 1:30 (Demonstração da Tela Funcional):**
  > *"Na tela principal, temos os 4 KPIs: Faturamento Líquido, Concluídos, Cancelamentos e Ticket Médio. Abaixo vemos o gráfico de tendência de vendas diárias e o gráfico de rosca com a situação dos pedidos. Na parte inferior, temos o ranking dos itens mais vendidos. Ao clicar nos botões de filtro ou selecionar datas, os 4 endpoints são consultados em paralelo sem recarregar a tela, respeitando o limite de segurança de até 1 ano."*
* **1:30 - 2:20 (API, Validações e Testes):**
  > *"Os endpoints REST `/api/dashboard/*` são protegidos por `@PreAuthorize` e validam regras como datas invertidas e limite de 366 dias retornando HTTP 400. Temos uma suíte com 13 testes automatizados (unitários e MockMvc) cobrindo regras de negócio e segurança com 100% de sucesso."*
* **2:20 - 3:00 (Conclusão e Gitflow):**
  > *"O código foi desenvolvido na branch `feature/dashboard`, com commits semânticos e integrado ao MongoDB. A solução entrega visibilidade em tempo real para apoiar a tomada de decisão do estabelecimento."*

