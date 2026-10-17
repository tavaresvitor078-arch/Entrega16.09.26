# PediuPatrão - Sistema de Gestão de Pizzaria

Este repositório contém a implementação da aplicação **PediuPatrão**, desenvolvida em Java com Spring Boot, Spring Security, MongoDB e Thymeleaf.

## 🚀 Tecnologias Utilizadas
- **Java 17 / Spring Boot**
- **Spring Security** (Autenticação e Perfis)
- **MongoDB** (Persistência de Dados)
- **Thymeleaf & Bootstrap** (Front-end)

## 🔑 Perfis de Acesso (RF01)
- **ADMIN:** Gestão geral e navegação.
- **GERENTE:** Gestão de clientes, pedidos e consultas.
- **ATENDENTE:** Operação e acompanhamento de clientes e pedidos.

## 🛠️ Como Executar
1. Certifique-se de que o MongoDB está em execução (`localhost:27017`).
2. Execute o projeto com o comando:
   ```bash
   mvn spring-boot:run
