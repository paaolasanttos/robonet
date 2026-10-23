# ROBONET — Sistema de Gerenciamento de Aulas

Aplicação web para gerenciamento e acompanhamento de aulas, usuários e atividades acadêmicas, com controle de acesso por perfil, autenticação em duas etapas por código enviado por e-mail, aceite de termo de uso/privacidade e registro de auditoria.

## Sumário

- [Visão geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Funcionalidades](#funcionalidades)
- [Perfis de acesso](#perfis-de-acesso)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Execução com Docker Compose](#execução-com-docker-compose)
- [Execução manual](#execução-manual)
- [Banco de dados](#banco-de-dados)
- [Autenticação e segurança](#autenticação-e-segurança)
- [Configuração do Gmail](#configuração-do-gmail)
- [Principais endpoints](#principais-endpoints)
- [Frontend](#frontend)
- [Backend](#backend)
- [Solução de problemas](#solução-de-problemas)
- [Observações](#observações)

---

## Visão geral

O ROBONET é composto por três partes principais:

1. **Frontend** — aplicação React responsável pela interface do usuário.
2. **Backend** — API REST desenvolvida com Spring Boot.
3. **Banco de dados** — PostgreSQL, inicializado pelos scripts SQL do diretório `database`.

A aplicação possui diferentes experiências de acordo com o perfil do usuário:

- **Administrador**: gerenciamento de usuários, aulas e consulta de auditoria.
- **Professor**: gerenciamento de aulas.
- **Aluno**: visualização e conclusão das aulas disponibilizadas.

O acesso utiliza JWT e as operações protegidas exigem autenticação. O aluno também precisa ter aceitado a versão vigente do termo de uso/privacidade para acessar suas aulas.

---

## Tecnologias

### Frontend

- React 18
- React DOM 18
- React Router DOM 6
- Vite 6
- Nginx para servir a aplicação em produção/container

### Backend

- Java 17
- Spring Boot 3.3.5
- Spring Web
- Spring Validation
- Spring Data JPA
- Spring Security
- JWT — JJWT 0.12.6
- PostgreSQL Driver
- Google Gmail API
- Jakarta Mail / Angus Mail
- Maven

### Infraestrutura

- Docker
- Docker Compose
- PostgreSQL 16 Alpine
- Nginx

---

## Arquitetura

```text
┌──────────────────────┐
│      Navegador       │
│   React + Vite       │
└──────────┬───────────┘
           │ HTTP/REST
           │ JWT Bearer
           ▼
┌──────────────────────┐
│       Backend        │
│ Spring Boot :4000    │
│ Security + JPA       │
└──────────┬───────────┘
           │ JDBC/JPA
           ▼
┌──────────────────────┐
│      PostgreSQL      │
│        :5432         │
└──────────────────────┘

Backend ───────────────► Gmail API
          códigos de acesso
```

Em Docker Compose, as portas padrão expostas são:

| Serviço | Porta do host | Porta interna |
|---|---:|---:|
| Frontend | `8080` | `80` |
| Backend | `4000` | `4000` |
| PostgreSQL | `5433` | `5432` |

---

## Funcionalidades

### Autenticação

- Login com e-mail e senha na primeira etapa.
- Envio de código de 6 dígitos por e-mail.
- Validação do código.
- Geração de token JWT.
- Logout.
- Recuperação de senha por código enviado por e-mail.
- Senhas armazenadas utilizando BCrypt.
- Expiração e controle de reenvio dos códigos.

### Usuários

Administradores podem:

- Listar usuários.
- Criar usuários.
- Alterar usuários.
- Alterar perfil.
- Ativar/desativar usuários.
- Desativar usuários por exclusão lógica.
- Definir senha.
- Associar RGM aos alunos.

Perfis disponíveis:

- `admin`
- `professor`
- `aluno`

### Aulas

Administradores e professores podem:

- Listar aulas.
- Consultar uma aula por ID.
- Listar temas.
- Criar aulas.
- Atualizar aulas.
- Excluir aulas.

Quando uma aula é criada, o banco possui uma trigger que gera automaticamente o vínculo da aula com os alunos ativos.

### Área do aluno

Alunos que aceitaram o termo vigente podem:

- Visualizar suas aulas.
- Marcar uma aula como concluída.

### Termo de uso e privacidade

O sistema registra o aceite do termo por:

- Usuário.
- Versão do termo.
- IP.
- Data/hora.

A versão atual é configurada pela propriedade `TERMO_VERSAO`, com valor padrão `0.1`.

### Auditoria

São registrados eventos relacionados a:

- Autenticação.
- Falhas de login.
- Envio de códigos.
- Alteração de senha.
- Criação/alteração de usuários.
- Alteração de perfil.
- Ativação/inativação de usuários.
- Ações relacionadas a autorização.
- Outros eventos de segurança e operação.

A consulta dos registros de auditoria é restrita ao perfil `admin`.

---

## Perfis de acesso

| Recurso | Admin | Professor | Aluno |
|---|:---:|:---:|:---:|
| Acessar sistema | ✓ | ✓ | ✓ |
| Gerenciar aulas | ✓ | ✓ | — |
| Gerenciar usuários | ✓ | — | — |
| Consultar auditoria | ✓ | — | — |
| Visualizar aulas próprias | — | — | ✓ |
| Concluir aulas | — | — | ✓ |
| Aceitar/recusar termo | ✓ | ✓ | ✓ |

> O controle efetivo de autorização é realizado no backend. O frontend também protege as rotas de acordo com o perfil para melhorar a experiência de navegação.

---

## Estrutura do projeto

```text
entrega2809/
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/br/com/aulas/
│           │   ├── config/
│           │   ├── controller/
│           │   ├── dto/
│           │   ├── exception/
│           │   ├── model/
│           │   ├── repository/
│           │   ├── security/
│           │   └── service/
│           └── resources/
│               └── application.properties
│
├── database/
│   ├── 01-create.sql
│   └── 02-insert.sql
│
├── frontend/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   ├── package-lock.json
│   └── src/
│       ├── componentes/
│       ├── context/
│       ├── pagina/
│       ├── servicos/
│       ├── app.jsx
│       ├── main.jsx
│       └── style/
│
├── .env.example
└── docker-compose.yml
```

---

## Pré-requisitos

Para execução com Docker:

- Docker
- Docker Compose

Para execução manual:

- Java 17
- Maven
- Node.js compatível com o projeto
- npm
- PostgreSQL 16 ou compatível

---

## Configuração

### 1. Criar o arquivo `.env`

Na raiz do projeto:

```bash
cp .env.example .env
```

No Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Depois revise os valores do `.env`.

### Principais variáveis

| Variável | Descrição | Exemplo |
|---|---|---|
| `DB_HOST` | Host do PostgreSQL | `localhost` |
| `DB_PORT` | Porta do PostgreSQL | `5433` |
| `DB_NAME` | Nome do banco | `robonet` |
| `DB_USER` | Usuário do banco | `postgres` |
| `DB_PASSWORD` | Senha do banco | definida localmente |
| `APP_PORT` | Porta do frontend | `8080` |
| `BACKEND_PORT` | Porta do backend | `4000` |
| `JWT_SECRET` | Chave de assinatura do JWT | segredo aleatório |
| `JWT_EXPIRATION` | Expiração do JWT em milissegundos | `3600000` |
| `ADMIN_EMAIL` | E-mail do administrador inicial | `robonet.umc@gmail.com` |
| `ADMIN_PASSWORD` | Senha do administrador inicial | definida localmente |
| `GMAIL_ENABLED` | Ativa envio de códigos | `true`/`false` |
| `GMAIL_SENDER` | Conta remetente do Gmail | e-mail |
| `GMAIL_CLIENT_ID` | Client ID OAuth do Google | segredo OAuth |
| `GMAIL_CLIENT_SECRET` | Client Secret OAuth do Google | segredo OAuth |
| `GMAIL_REFRESH_TOKEN` | Refresh Token OAuth | token |
| `VITE_API_URL` | URL da API usada pelo frontend | `http://localhost:4000` |

### Segurança

Não versionar o arquivo `.env` com senhas, tokens, chaves JWT ou credenciais OAuth.

Para ambientes reais, substitua todos os valores de desenvolvimento por credenciais e segredos apropriados.

---

## Execução com Docker Compose

### Subir toda a aplicação

Na raiz do projeto:

```bash
docker compose up --build
```

Após a inicialização:

- Frontend: http://localhost:8080
- Backend: http://localhost:4000
- PostgreSQL: `localhost:5433`

### Executar em segundo plano

```bash
docker compose up --build -d
```

### Ver logs

```bash
docker compose logs -f
```

Somente backend:

```bash
docker compose logs -f backend
```

Somente banco:

```bash
docker compose logs -f postgres
```

### Parar os serviços

```bash
docker compose down
```

### Remover também o volume do PostgreSQL

```bash
docker compose down -v
```

> Atenção: `down -v` remove o volume `postgres_data` e, consequentemente, os dados persistidos nesse volume.

---

## Primeiro acesso

O banco possui um registro inicial de administrador com:

```text
E-mail: robonet.umc@gmail.com
```

A senha do administrador é definida pelo backend através da variável:

```text
ADMIN_PASSWORD
```

O valor padrão utilizado pelo `docker-compose.yml` em ambiente de desenvolvimento é:

```text
1234
```
Este valor será utilizado durante o desenvolvimento e depois será modificado.

**Recomenda-se alterar essa senha antes de qualquer uso fora de ambiente local.**

### Importante sobre o login

O fluxo atual não utiliza somente e-mail e senha.

O login ocorre em duas etapas:

```text
1. E-mail + senha
        ↓
2. Código de 6 dígitos enviado por e-mail
        ↓
3. Validação do código
        ↓
4. JWT
        ↓
5. Acesso ao sistema
```

Portanto, para o fluxo completo funcionar, o serviço Gmail deve estar configurado e habilitado.

---

## Banco de dados

O PostgreSQL é inicializado pelos arquivos montados em:

```text
./database:/docker-entrypoint-initdb.d:ro
```

Os scripts são executados na criação inicial do banco.

### `01-create.sql`

Cria as principais tabelas:

- `Usuarios`
- `TermosAceites`
- `AulasTemas`
- `Aulas`
- `AulasAluno`
- `LogAcessos`
- `LogCodigoVerificacao`
- `LogAuditoria`

Também cria índices, chaves estrangeiras, funções e triggers.

### `02-insert.sql`

Insere dados iniciais:

- Usuário administrador.
- Temas de aula:
  - Introdução à Robótica
  - Programação
  - Projeto final

### Relacionamentos principais

```text
Usuarios
   │
   ├──────────────► TermosAceites
   │
   └──────────────► AulasAluno ◄──────── Aulas
                                      │
                                      ▼
                                  AulasTemas
```

Ao cadastrar uma aula, uma trigger cria registros em `AulasAluno` para os alunos ativos.

---

## Autenticação e segurança

A API utiliza Spring Security com autenticação stateless.

### JWT

Após a validação do código enviado por e-mail, o backend gera um JWT contendo informações como:

- e-mail/identificador do usuário;
- perfil;
- indicação de aceite do termo;
- data de emissão;
- data de expiração.

O frontend envia o token nas requisições:

```http
Authorization: Bearer <token>
```

### Senhas

As senhas são armazenadas usando:

```text
BCrypt
```

O sistema não deve armazenar senhas em texto puro.

### Autorização

O backend utiliza `@PreAuthorize` e authorities/roles para restringir recursos.

Exemplos:

```text
ADMIN
PROFESSOR
ALUNO
TERMO_ACEITO
```

### CORS

As origens permitidas são configuradas pela variável:

```text
CORS_ORIGINS
```

Por padrão, o projeto considera ambientes locais em:

```text
http://localhost:8080
http://localhost:5173
http://localhost:3000
```

---

## Configuração do Gmail

Para habilitar o envio de códigos de acesso:

```env
GMAIL_ENABLED=true
GMAIL_SENDER=seu-email@gmail.com
GMAIL_CLIENT_ID=...
GMAIL_CLIENT_SECRET=...
GMAIL_REFRESH_TOKEN=...
```

O backend utiliza a API do Gmail com OAuth 2.0.

O código de acesso possui, por padrão:

```text
Expiração: 600 segundos (10 minutos)
Reenvio: intervalo mínimo de 60 segundos
```

Esses valores podem ser alterados por:

```env
GMAIL_CODE_EXPIRATION_SECONDS=600
GMAIL_RESEND_COOLDOWN_SECONDS=60
```

Se o Gmail estiver desabilitado ou sem as credenciais necessárias, a solicitação de código retornará erro de serviço.

---

## Principais endpoints

A API é disponibilizada, por padrão, em:

```text
http://localhost:4000
```

### Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/auth/login` | Endpoint legado; o fluxo atual utiliza código por e-mail |
| `POST` | `/auth/request-code` | Valida e-mail/senha e solicita código |
| `POST` | `/auth/verify-code` | Valida código e gera JWT |
| `POST` | `/auth/logout` | Registra logout |
| `POST` | `/auth/password-code` | Solicita código de recuperação de senha |
| `POST` | `/auth/reset-password` | Redefine senha |

### Aulas

Requer perfil `admin` ou `professor`.

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/aulas` | Lista aulas |
| `GET` | `/aulas/{id}` | Consulta aula |
| `GET` | `/aulas/temas` | Lista temas |
| `POST` | `/aulas` | Cria aula |
| `PUT` | `/aulas/{id}` | Atualiza aula |
| `DELETE` | `/aulas/{id}` | Exclui aula |

### Aulas do aluno

Requer perfil `aluno` e aceite do termo vigente.

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/aulas-aluno` | Lista aulas do aluno |
| `POST` | `/api/aulas-aluno/concluir` | Marca aula como concluída |

Exemplo de payload:

```json
{
  "aulaId": 1
}
```

### Usuários

Requer perfil `admin`.

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/usuarios` | Lista usuários |
| `POST` | `/api/usuarios` | Cria usuário |
| `PUT` | `/api/usuarios/{id}` | Atualiza usuário |
| `PUT` | `/api/usuarios/{id}/perfil` | Altera perfil |
| `PATCH` | `/api/usuarios/{id}/ativo` | Ativa/desativa usuário |
| `DELETE` | `/api/usuarios/{id}` | Desativa usuário |

### Termo

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/termo/aceitar` | Registra aceite e renova JWT |
| `POST` | `/api/termo/recusar` | Registra recusa |

### Auditoria

Requer perfil `admin`.

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/auditoria` | Lista registros de auditoria |

---

## Frontend

O frontend está localizado em:

```text
frontend/
```

### Desenvolvimento

Entre no diretório:

```bash
cd frontend
```

Instale as dependências:

```bash
npm install
```

Execute:

```bash
npm run dev
```

Por padrão, o Vite disponibiliza a aplicação em uma porta local, normalmente `5173`.

A URL da API pode ser configurada por:

```env
VITE_API_URL=http://localhost:4000
```

### Build

```bash
npm run build
```

### Preview

```bash
npm run preview
```

### Principais páginas

O frontend possui páginas para:

- Login.
- Recuperação de senha.
- Home.
- Aulas.
- Aulas do aluno.
- Usuários.
- Auditoria.
- Termo.
- Políticas.
- Acesso negado.

As rotas protegidas utilizam o componente:

```text
frontend/src/componentes/RotaProtegida.jsx
```

---

## Backend

O backend está localizado em:

```text
backend/
```

### Desenvolvimento

Entre no diretório:

```bash
cd backend
```

Compile o projeto:

```bash
mvn clean package
```

Execute:

```bash
mvn spring-boot:run
```

Ou execute o JAR gerado:

```bash
java -jar target/aulas-backend-1.0.0.jar
```

A API será disponibilizada na porta:

```text
4000
```

### Principais camadas

```text
controller/
    Endpoints REST

service/
    Regras de negócio

repository/
    Acesso aos dados

model/
    Entidades JPA

dto/
    Objetos de entrada/saída

security/
    JWT e autenticação

config/
    Configurações e inicialização

exception/
    Tratamento de exceções
```

---

## Solução de problemas

### Frontend não consegue acessar o backend

Verifique:

```env
VITE_API_URL=http://localhost:4000
```

Também verifique se o backend está em execução:

```bash
docker compose ps
```

E consulte os logs:

```bash
docker compose logs -f backend
```

### Erro relacionado ao Gmail

Confirme:

```env
GMAIL_ENABLED=true
GMAIL_SENDER=...
GMAIL_CLIENT_ID=...
GMAIL_CLIENT_SECRET=...
GMAIL_REFRESH_TOKEN=...
```

Se o envio de e-mail não for necessário no ambiente de desenvolvimento, lembre-se de que o fluxo atual de login depende do código enviado por e-mail.

### Banco não foi recriado

Os scripts de `database/` são executados automaticamente pelo PostgreSQL somente quando o banco/volume é inicializado.

Se for necessário recriar o banco em ambiente de desenvolvimento:

```bash
docker compose down -v
docker compose up --build
```

**Atenção:** isso remove os dados persistidos.

### JWT inválido ou erro ao iniciar o backend

A variável `JWT_SECRET` deve possuir pelo menos 32 bytes.

Exemplo:

```env
JWT_SECRET=uma-chave-local-longa-e-aleatoria-com-pelo-menos-32-bytes
```

Não utilize uma chave de desenvolvimento em produção.

---

## Observações

- O projeto contém um diretório `.git` dentro do pacote original. Ele é metadado do repositório e não é necessário para executar a aplicação.
- O arquivo `.env.example` deve ser copiado para `.env` antes de utilizar o `docker-compose.yml`, pois o serviço backend referencia `.env` como `env_file`.
- O `docker-compose.yml` possui valores padrão voltados a desenvolvimento. Eles devem ser revisados antes de qualquer implantação real.
- O banco utiliza `spring.jpa.hibernate.ddl-auto=validate`, portanto a estrutura esperada pelo Hibernate deve existir no PostgreSQL.
- O endpoint `/auth/login` está mantido no código, mas retorna HTTP `410`, informando que o login atual exige código enviado por e-mail.
- Não foram identificados testes automatizados no conteúdo do pacote; o `pom.xml` possui a dependência de testes do Spring Boot, mas não há diretório de testes apresentado na estrutura do projeto.
- O projeto utiliza exclusão lógica de usuários: a operação de exclusão altera o status para inativo.
- A aplicação foi estruturada para uso local/containerizado e requer revisão de credenciais, CORS, segredo JWT, senha administrativa e integração Gmail antes de um ambiente de produção.

---
