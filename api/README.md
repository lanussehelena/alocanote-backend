💻 AlocaNote — Backend (API RESTful)

Sistema Centralizado para Gestão de Reservas de Notebooks Corporativos

O AlocaNote é uma solução desenvolvida para otimizar e organizar a alocação de notebooks entre colaboradores de uma loja de móveis planejados em João Pessoa/PB. Ele substitui a gestão informal feita anteriormente via grupos de WhatsApp por um fluxo automatizado, transparente e seguro.

🛠️ Tecnogias e Ferramentas

Linguagem: Java 17+

Framework: Spring Boot 3.x

Segurança: Spring Security + JWT (JSON Web Token) + Firebase Authentication (Conta Google e E-mail/Senha)

Persistência de Dados: Spring Data JPA / Hibernate

Banco de Dados (Desenvolvimento/Testes): H2 Database (In-Memory)

Documentação & Ferramentas: Postman / Maven

📋 Regras de Negócio e Funcionalidades

Gestão de Usuários e Autenticação:

Cadastro e Login de colaboradores associados a papéis específicos (ADMINISTRADOR, PROJETISTA, CONFERENTE, CONSULTOR_VENDAS, OUTROS).

Autenticação por e-mail e senha manual ou via Conta Google integrada com o Firebase Authentication.

Autenticação baseada em tokens JWT e Firebase ID Tokens para rotas protegidas.

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


A API estará disponível por padrão em: http://localhost:8082

Acessar o Banco de Dados H2 Console:

URL: http://localhost:8082/h2-console

JDBC URL: jdbc:h2:file:./data/alocanotedb

User: sa

Password: (deixar em branco)

📌 Endpoints Principais (API)

🔐 Autenticação (/api/auth)

POST /api/auth/register — Cadastra manualmente um novo colaborador com e-mail e senha.

POST /api/auth/login — Autentica o usuário com e-mail e senha e retorna o token JWT.

POST /api/auth/firebase (ou POST /api/auth/google) — Autentica/sincroniza o usuário via token do Firebase Authentication (Conta Google ou E-mail/Senha) e retorna o token JWT.

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