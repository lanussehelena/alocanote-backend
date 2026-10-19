💻 AlocaNote — Backend (API RESTful)

Sistema Centralizado para Gestão de Reservas de Notebooks Corporativos

O AlocaNote é uma solução desenvolvida para otimizar e organizar a alocação de notebooks entre colaboradores de uma loja de móveis planejados em João Pessoa/PB. Ele substitui a gestão informal feita anteriormente via grupos de WhatsApp por um fluxo automatizado, transparente e seguro.

🛠️ Tecnogias e Ferramentas

Linguagem: Java 17+

Framework: Spring Boot 3.x

Segurança: Spring Security + JWT (JSON Web Token) + Verificação em 2 Etapas (Token SMS Mock)

Persistência de Dados: Spring Data JPA / Hibernate

Banco de Dados (Desenvolvimento/Testes): H2 Database (In-Memory)

Documentação & Ferramentas: Postman / Maven

📋 Regras de Negócio e Funcionalidades

Gestão de Usuários e Autenticação:

Cadastro e Login de colaboradores associados a papéis específicos (ADMINISTRADOR, PROJETISTA, CONFERENTE, CONSULTOR_VENDAS, OUTROS).

Verificação de segurança via token de 6 dígitos enviado por SMS (Mock) antes da liberação do acesso.

Autenticação baseada em tokens JWT para rotas protegidas.

Gestão de Equipamentos (Notebooks):

Rastreamento em tempo real do status do equipamento (DISPONIVEL, EM_USO, MANUTENCAO).

Associação automática do usuário que está utilizando o equipamento no momento.

Reservas e Alocações:

Travas de segurança para evitar agendamentos duplicados ou sobrepostos.

Histórico de retiradas e devoluções para auditoria e controle interno.

🚀 Como Executar o Projeto Localmente

Pré-requisitos

Java JDK 17 ou superior instalado.

Maven instalado (ou utilizar o wrapper ./mvnw).

IDE da sua preferência (VS Code, IntelliJ IDEA, Eclipse).

Passo a Passo

Clonar o repositório:

git clone https://github.com/seu-usuario/alocanote-backend.git
cd alocanote-backend


Compilar e executar a aplicação:

./mvnw spring-boot:run


A API estará disponível por padrão em: http://localhost:8080

Acessar o Banco de Dados H2 Console:

URL: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:alocanotedb

User: sa

Password: (deixar em branco)

📌 Endpoints Principais (API)

🔐 Autenticação (/api/auth)

POST /api/auth/register — Cadastra um novo colaborador.

POST /api/auth/verify-token — Valida o código SMS de 6 dígitos.

POST /api/auth/login — Autentica o usuário e retorna o token JWT.

💻 Notebooks (/api/notebooks)

GET /api/notebooks — Lista todos os notebooks e seus status atuais.

GET /api/notebooks/{id} — Retorna detalhes de um equipamento específico.

POST /api/notebooks — Cadastra um novo notebook (Requer perfil ADMIN).

📅 Reservas (/api/reservations)

GET /api/reservations — Lista todas as reservas ativas/futuras.

POST /api/reservations — Realiza um novo agendamento com validação de horários.

👭 Equipe de Desenvolvimento

Projeto desenvolvido para a Unidade Curricular de Programação para Dispositivos Móveis & Projeto Extensionista IV (Centro Universitário UNIESP).

Lanusse Silveira — Desenvolvedora

Tamires Carvalho — Desenvolvedora