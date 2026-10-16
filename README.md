# PediuPatrão — Sistema de Gestão de Pedidos e Restaurante

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
| **`ADMIN`** | Administrar usuários, atribuir perfis (`ADMIN`, `GERENTE`, `ATENDENTE`), consultar pedidos e visualizar auditoria. |
| **`GERENTE`** | Aplicar descontos (até 20%), cancelar pedidos com justificativa, consultar clientes, gerenciar status e visualizar auditoria. |
| **`ATENDENTE`** | Cadastrar e atualizar clientes, registrar pedidos no sistema e acompanhar o andamento dos pedidos. |

## Principais Alterações Realizadas no Projeto

### Backend (Java / Spring)
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
