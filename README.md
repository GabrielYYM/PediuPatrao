# PediuPatrão — Sistema de Gestão de Pedidos e Restaurante

**Vídeo de Apresentação (Demonstração da Solução):** [Assistir no Google Drive](https://drive.google.com/file/d/17of1apuNTNT6wXU5hLLVZ_TnHRFGBGxQ/view?usp=sharing)

Sistema web para gerenciamento operacional de pizzarias e restaurantes, construído com **Spring Boot 3**, **MongoDB** e **Spring Security**. O sistema conta com controle de acesso baseado em papéis (RBAC), máquina de estados para ciclo de vida de pedidos, cálculo de preços e descontos no servidor e auditoria granular persistente.

---

## Tecnologias Utilizadas

* **Linguagem:** Java 21
* **Framework:** Spring Boot 3.4.4
  * Spring Web
  * Spring Data MongoDB
  * Spring Security
  * Thymeleaf
* **Banco de Dados:** MongoDB Atlas
* **Front-end:** HTML5, CSS3, JavaScript e Bootstrap
* **Gerenciador de Dependências:** Apache Maven

---

## Instruções de Execução

### 1. Pré-requisitos
* **Java Development Kit (JDK) 21** instalado e configurado no `PATH`.
* Instância do **MongoDB Atlas** ativa e conectada.

### 2. Configuração do Banco de Dados
Verifique o arquivo `src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/pediupatrao
# Ou a URI de conexão do seu MongoDB Atlas
   ```

### 3. Compilação e Execução
Execute o Maven Wrapper a partir do terminal na raiz do projeto:

* **Windows (PowerShell ou CMD):**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```

## Perfis de Acesso e Responsabilidades (RBAC)

O sistema conta com três perfis estritos de acesso definidos em `UserRole`:

| Perfil | Responsabilidades Principais |
| :--- | :--- |
| **`ADMIN`** | Administrar usuários, atribuir perfis (`ADMIN`, `GERENTE`, `ATENDENTE`), consultar pedidos, acessar Dashboard Administrativo e visualizar auditoria. |
| **`GERENTE`** | Aplicar descontos (até 20%), cancelar pedidos com justificativa, consultar clientes, gerenciar status, acompanhar Dashboard Administrativo e visualizar auditoria. |
| **`ATENDENTE`** | Cadastrar e atualizar clientes, registrar pedidos no sistema e acompanhar o andamento dos pedidos. |

---

## Dashboard Administrativo

Módulo analítico integrado ao sistema de pedidos para acompanhamento de desempenho operacional, faturamento e apoio à tomada de decisões do estabelecimento.

### Requisitos Funcionais (RF) Implementados

* **RF01 — KPI:**
  * Cálculo em tempo real do faturamento líquido total (somente pedidos concluídos/finalizados).
  * Contagem total de pedidos finalizados com sucesso.
  * Contagem de pedidos cancelados.
  * Cálculo dinâmico do Ticket Médio (`Faturamento / Pedidos Finalizados`) com tratamento para evitar divisão por zero.
* **RF02 — Análise de Vendas:**
  * Agrupamento cronológico diário com somatório de receita e volume de pedidos para visualização de tendências.
* **RF03 — Situação dos Pedidos:**
  * Distribuição quantitativa de pedidos agrupados por status do ciclo de vida (`RECEBIDO`, `EM_PREPARACAO`, `PRONTO`, `RETIRADO`, `FINALIZADO`, `CANCELADO`).
* **RF04 — Produtos Mais Vendidos:**
  * Ranking ordenado decrescente dos produtos com maior volume de saída e vendas acumuladas a partir dos itens do pedido.
* **RF05 — Filtros de Consulta:**
  * Filtragem flexível por período via Query Parameters.
  * Fallback inteligente: caso nenhum período seja informado, adota automaticamente os **últimos 30 dias**.
  * **Proteção contra consultas excessivas:** Validação estrita de intervalo máximo de **1 ano (366 dias)** tanto no front-end quanto no back-end (rejeitando com `HTTP 400 Bad Request` se ultrapassado ou com datas invertidas).
* **RF06 — Integração com a API:**
  * Endpoints RESTful desacoplados que fornecem contratos JSON semânticos baseados em Java Records imutáveis.
* **RF07 — Atualização e Tratamento de Dados:**
  * Tratamento estrito de valores monetários com `java.math.BigDecimal` e arredondamento financeiro (`RoundingMode.HALF_UP`).
  * Isolamento de pedidos cancelados das métricas de receita para garantir fidedignidade contábil.

### Requisitos Não Funcionais (RNF) Implementados

* **RNF01 — Segurança e Autorização (RBAC):** Proteção no nível de método com anotação `@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")`. Usuários sem permissão recebem `HTTP 403 Forbidden` e acessos não autenticados são barrados.
* **RNF02 — Performance e Otimização de Banco NoSQL:** Consulta indexada por intervalo de datas diretamente no MongoDB (`findByDataHoraEntradaBetween`), evitando overhead de tráfego de rede e sobrecarga de memória.
* **RNF03 — Precisão e Integridade Numérica:** Eliminação de erros de ponto flutuante em cálculos de faturamento e ticket médio através do uso de `BigDecimal`.
* **RNF04 — Testabilidade e Confiabilidade:** 100% dos fluxos cobertos por suíte de testes automatizados com JUnit 5, Mockito e MockMvc.
* **RNF05 — Arquitetura Limpa e Manutenibilidade:** Separação clara de responsabilidades entre DTOs (`record`), Service e Controller REST, seguindo o padrão de projeto arquitetural existente.

### Documentação dos Endpoints REST (`/api/dashboard`)

| Método | Endpoint | Descrição | Parâmetros de Query | Permissão |
| :---: | :--- | :--- | :--- | :---: |
| `GET` | `/api/dashboard/resumo` | Retorna faturamento, finalizados, cancelados e ticket médio | `inicio`, `fim` (opcionais, AAAA-MM-DD) | `ADMIN`, `GERENTE` |
| `GET` | `/api/dashboard/vendas` | Série temporal de vendas e quantidade de pedidos por dia | `inicio`, `fim` (opcionais, AAAA-MM-DD) | `ADMIN`, `GERENTE` |
| `GET` | `/api/dashboard/status` | Distribuição quantitativa de pedidos por status | `inicio`, `fim` (opcionais, AAAA-MM-DD) | `ADMIN`, `GERENTE` |
| `GET` | `/api/dashboard/ranking-produtos` | Lista dos produtos mais vendidos ordenados | `inicio`, `fim` (opcionais), `limite` (padrão: 5) | `ADMIN`, `GERENTE` |

---

## Principais Alterações Realizadas no Projeto

### Backend (Java / Spring)
* **`dto/DashboardResumoDTO.java`, `VendaPeriodoDTO.java`, `PedidoStatusDTO.java`, `ProdutoRankingDTO.java`:** DTOs padronizados com Java Records e `BigDecimal`.
* **`service/DashboardService.java`:** Lógica de negócio, métricas financeiras, cálculo de ticket médio e rankings analíticos.
* **`controller/DashboardController.java`:** Endpoints REST `/api/dashboard/*` protegidos por perfil com Spring Security.
* **`repository/PedidoRepository.java`:** Adicionada consulta `findByDataHoraEntradaBetween` para filtros temporais otimizados.
* **`test/DashboardServiceTest.java` & `DashboardControllerTest.java`:** 11 testes automatizados (unitários e de integração com MockMvc).
* **`entity/enums/UserRole.java`:** Criação do enum com `ADMIN`, `GERENTE` e `ATENDENTE`.
* **`entity/enums/StatusPedido.java`:** Criação da máquina de estados do pedido.
* **`entity/ItemsPedido.java`:** Objeto embutido para itens do pedido (produtoId, nome, quantidade, precoUnidade, total).
* **`entity/Pedido.java`:** Atualizado com itens, status tipado, desconto, valorTotal, datas e responsáveis de entrada/saída/cancelamento.
* **`entity/Auditoria.java` & `repository/AuditoriaRepository.java`:** Criação da entidade e repositório NoSQL de logs.
* **`service/AuditoriaService.java`:** Serviço centralizador de auditoria com extração automática do contexto de segurança.
* **`service/PedidoService.java`:** Validação de fluxo linear, cálculos no servidor, travas de desconto e cancelamento.
* **`service/ClienteService.java`:** Detecção de alterações campo a campo em dados cadastrais e de endereço.
* **`controller/PedidoController.java`:** Inclusão dos endpoints específicos de status, desconto e cancelamento com `@PreAuthorize`.
* **`controller/AuditoriaController.java`:** Endpoints de consulta de auditoria restritos a Admin e Gerente.
* **`config/SecurityConfig.java`:** Habilitação do `@EnableMethodSecurity` e remoção do bypass público em `/api/usuarios`.

### Front-end (Thymeleaf / HTML / Bootstrap)
* **`templates/pedidos.html`:** Refatorado para exibir itens detalhados, valor total, desconto, data/hora, responsável e badges coloridos para todos os novos status, prevenindo erros de carregamento.
* **`templates/usuarios/form.html` & `lista.html`:** Atualizado o formulário e a listagem para contemplar os perfis `ADMIN`, `GERENTE` e `ATENDENTE`.
* **`templates/auditoria.html`:** Nova tela responsiva para visualização tabular de todos os eventos auditados.
* **`templates/fragments/sidebar.html`:** Inclusão do link de acesso rápido para **Auditoria** no menu lateral.
* **`controller/HomeController.java`:** Adicionada a rota `/auditoria` para servir a interface aos gestores.
