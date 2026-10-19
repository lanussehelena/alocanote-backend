# AGENTS.md - Guia do Desenvolvedor & Agentes de IA (Backend - AlocaNote)

Este documento define a arquitetura, regras de negócio e padrões técnicos para o desenvolvimento do backend do **AlocaNote** (API Spring Boot).

## 🏢 Contexto de Domínio
O **AlocaNote** é um sistema de gestão e reserva de ativos corporativos desenvolvido para a equipe interna de uma loja de móveis planejados em João Pessoa. O sistema substitui o controle informal via WhatsApp por um fluxo automatizado e centralizado.
* **Ativos geridos:** 3 notebooks compartilhados entre os colaboradores.
* **Público-alvo interno:** 8 colaboradores divididos entre setores como Vendas (Consultores e Projetistas) e Conferência Técnica.

## 🛠️ Stack Tecnológica Permitida
- **Linguagem:** Java 21
- **Framework:** Spring Boot 3+
- **Segurança:** Spring Security com tokens JWT (`JwtTokenProvider`)
- **Persistência:** Spring Data JPA / Hibernate (H2 Database para ambiente de desenvolvimento/testes)
- **Validação:** Jakarta Validation (`@Valid`, `@NotNull`, `@NotBlank`, etc.)

## 📁 Arquitetura de Pastas Fixa (`src/main/java/com/alocanote/api/`)
- `config/`: Configurações globais (Segurança, CORS, Inicialização de dados via `DataInitializer`).
- `controller/`: Controladores REST expostos (`@RestController`, `@RequestMapping`). Devem apenas receber requisições, validar DTOs e chamar os serviços.
- `service/`: Regras de negócio da aplicação (ex: `AuthService`, `NotebookService`, `ReservationService`, `SmsService`).
- `repository/`: Interfaces de acesso a dados estendendo `JpaRepository`.
- `model/entity/`: Entidades JPA mapeadas para o banco de dados (`User`, `Notebook`, `Reservation`, `VerificationToken`).
- `dto/`: Objetos de Transferência de Dados separados em `request/` e `response/`.
- `exception/`: Tratamento centralizado de erros (`GlobalExceptionHandler`, `BusinessException`, etc.).
- `security/`: Filtros de autenticação, implementação de `UserDetailsService` e gestão do JWT.
- `enums/`: Enumerações restritas do sistema.

## 📋 Regras de Negócio e Domínio

### 1. Papéis de Utilizador (`Role`)
O sistema restringe e valida permissões com base nos seguintes papéis espelhados no enum `Role`:
- `ADMINISTRADOR`
- `PROJETISTA`
- `CONFERENTE`
- `CONSULTOR_VENDAS`
- `OUTROS`

### 2. Estados dos Equipamentos (`NotebookStatus`)
Os notebooks possuem ciclos de vida bem definidos controlados pelo enum `NotebookStatus`:
- `DISPONIVEL`: Pronto para ser reservado.
- `EM_USO`: Atualmente alocado a um colaborador.
- `MANUTENCAO`: Indisponível temporariamente.

### 3. Autenticação e Segurança por SMS (Mock)
- O fluxo de autenticação suporta login padrão com e-mail e senha gerando um token JWT (`/api/auth/login`).
- O sistema possui um serviço de duplo fator simulado (`SmsService`), que gera tokens de verificação de 6 dias/dígitos e imprime um mock no console (`[SMS MOCK]`) para validação via `/api/auth/verify-sms`.

### 4. Prevenção de Conflitos
- O backend deve implementar travas lógicas para impedir agendamentos duplicados ou sobrepostos para o mesmo equipamento e faixa horária.

## 📐 Padrões de Código
- **Imutabilidade e Records:** Priorizar o uso de Java Records para DTOs e objetos de transferência simples.
- **Injeção de Dependências:** Utilizar construtores (evitar `@Autowired` em campos).
- **Tratamento de Exceções:** Lançar `BusinessException` para violações de regras de negócio, que serão tratadas de forma uniforme pelo `GlobalExceptionHandler`.
- **Commits:** Seguir rigorosamente o padrão *Conventional Commits* em português do Brasil (ex: `feat: criacao do endpoint de reservas`, `fix: ajuste no filtro de seguranca jwt`).