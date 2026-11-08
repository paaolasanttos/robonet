# ROBONET — Sistema de Gerenciamento de Aulas

Aplicação web para organizar aulas, simulados, turmas, equipes e torneios. O sistema oferece áreas diferentes para administradores, professores e alunos, com autenticação, autorização por perfil, aceite de termo de uso e registro de auditoria.

## Sumário

- [Visão geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Funcionalidades](#funcionalidades)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Execução com Docker Compose](#execução-com-docker-compose)
- [Execução local para desenvolvimento](#execução-local-para-desenvolvimento)
- [Banco de dados](#banco-de-dados)
- [Autenticação e autorização](#autenticação-e-autorização)
- [Configuração do Gmail](#configuração-do-gmail)
- [API](#api)
- [Testes e build](#testes-e-build)
- [Solução de problemas](#solução-de-problemas)

## Visão geral

O ROBONET é composto por três partes:

1. **Frontend**: aplicação de página única (SPA) que apresenta as telas e consome a API.
2. **Backend**: API REST que aplica as regras do sistema, autentica usuários e acessa os dados.
3. **Banco de dados**: PostgreSQL, cujo esquema e dados iniciais são definidos pelos scripts SQL em `database/`.

O frontend e o backend são aplicações independentes. Em produção com Docker Compose, o frontend é servido pelo Nginx, o backend é executado em um contêiner Java e o PostgreSQL persiste seus dados em um volume.

## Tecnologias

| Componente | Tecnologias |
|---|---|
| Frontend | React 18, React Router 6, Vite 6 |
| Backend | Java 17, Spring Boot 3.3, Spring Web, Spring Security, Spring Data JPA, Bean Validation |
| Autenticação | JWT e Spring Security |
| Banco de dados | PostgreSQL 16 |
| E-mail | Gmail API / OAuth, opcionalmente habilitado |
| Execução e publicação | Docker Compose, Docker e Nginx |

## Arquitetura

### Backend: arquitetura em camadas

O backend separa a entrada HTTP, as regras de negócio e a persistência. O fluxo habitual de uma operação é:

```text
Navegador
  -> serviço de API do frontend (fetch/JSON)
  -> Controller (endpoint REST)
  -> DTO (dados de entrada/saída)
  -> Service (regras e casos de uso)
  -> Repository (acesso a dados com Spring Data JPA)
  -> Model/Entity
  -> PostgreSQL
```

As camadas e seus papéis são:

- **`controller/` — apresentação HTTP**: publica os endpoints REST, recebe requisições e encaminha as operações para os serviços. Os controllers organizam os recursos, como autenticação, aulas, simulados, usuários, turmas, equipes, torneios, dados pessoais, dashboard e auditoria.
- **`dto/` — contratos da API**: representa os dados aceitos e retornados pela API. Os DTOs desacoplam os formatos HTTP das entidades persistidas.
- **`service/` — aplicação e domínio**: implementa os fluxos e regras de negócio, como gerenciamento de aulas e simulados, análise das solicitações de alteração dos dados, consultas do dashboard, auditoria, termos e torneios. O agendador de exclusão de conta também fica nessa área.
- **`repository/` — persistência**: interfaces Spring Data JPA usadas pelos serviços para consultar e gravar dados no PostgreSQL.
- **`model/` — modelo persistente**: entidades JPA que representam os dados e relacionamentos armazenados, como usuários, aulas, simulados, termos, auditoria, equipes e torneios.
- **`security/` — autenticação e políticas**: filtro e serviços JWT, contexto do usuário autenticado, códigos de verificação e política de senha.
- **`config/` — configuração da aplicação**: configuração de segurança e CORS, inicialização da conta administradora e componentes de integração.
- **`exception/` — tratamento de erros**: exceções da aplicação e tratamento global para respostas de erro da API.

O `AulasBackendApplication` inicializa o Spring Boot e habilita tarefas agendadas. A configuração `spring.jpa.hibernate.ddl-auto=validate` faz o Hibernate validar o esquema existente: a aplicação não cria as tabelas automaticamente.

### Frontend: SPA React

O frontend também possui responsabilidades separadas:

- **`src/pagina/`** contém as páginas associadas às rotas.
- **`src/componentes/`** contém componentes reutilizáveis, modais e proteção de rotas.
- **`src/context/AuthContext.jsx`** mantém a sessão de autenticação no navegador.
- **`src/servicos/api.js`** centraliza as chamadas HTTP, inclui o JWT quando há sessão e converte respostas de erro em mensagens para a interface.
- **`src/app.jsx`** declara as rotas da aplicação; `src/main.jsx` monta a SPA com React Router e o contexto de autenticação.

As proteções no frontend controlam a navegação e a experiência visual. A autorização efetiva também é aplicada no backend e não deve depender apenas da interface.

### Integração entre as camadas

1. O usuário interage com uma página React.
2. A página chama uma função do módulo de serviços, que envia uma requisição JSON para a API.
3. Para endpoints autenticados, o serviço inclui `Authorization: Bearer <token>` a partir da sessão do navegador.
4. O filtro JWT identifica o usuário; as regras do Spring Security e as verificações dos endpoints limitam o acesso.
5. O controller valida e encaminha a operação ao serviço de negócio.
6. O serviço usa um ou mais repositórios para ler ou gravar entidades no PostgreSQL e devolve dados em DTOs.
7. O frontend apresenta o resultado ao usuário.

## Funcionalidades

- Cadastro e administração de usuários, perfis, status e acesso por MFV.
- Autenticação, solicitação e verificação de código, recuperação de senha e encerramento de sessão.
- Consulta e gerenciamento de aulas; acompanhamento e conclusão de aulas pelo aluno.
- Criação e gerenciamento de simulados; realização e consulta de resultados pelo aluno.
- Gerenciamento de turmas e equipes.
- Criação e gerenciamento de torneios, chaveamento e registro de resultados das lutas.
- Dashboard com resumo das atividades.
- Consulta de auditoria das operações.
- Consulta e atualização dos dados pessoais, alteração de senha, exportação de dados e solicitação/cancelamento de exclusão de conta.
- Solicitações de alteração de dados pessoais submetidas à análise administrativa; o administrador pode deferir ou indeferir a solicitação.
- Aceite do termo de uso/privacidade.

As páginas e operações disponíveis dependem do perfil autenticado e das regras configuradas no backend.

## Estrutura do repositório

```text
.
├── backend/
│   ├── src/main/java/br/com/aulas/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-development.yml
│   │   └── application-production.yml
│   ├── src/test/
│   ├── Dockerfile
│   └── pom.xml
├── database/                 # Esquema, funções, gatilhos e dados iniciais
├── frontend/
│   ├── src/
│   │   ├── componentes/
│   │   ├── context/
│   │   ├── pagina/
│   │   ├── servicos/
│   │   └── style/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── .env.example
└── docker-compose.yml
```

## Pré-requisitos

Para executar com Docker Compose:

- Docker com suporte ao comando `docker compose`.

Para executar os serviços localmente:

- Java 17 e Maven.
- Node.js 20 ou compatível e npm.
- PostgreSQL 16, localmente ou iniciado pelo Docker Compose.

## Configuração

As configurações são fornecidas por variáveis de ambiente. O arquivo `.env.example` lista as variáveis esperadas; crie uma cópia local e preencha os valores. Não versione o `.env` nem credenciais reais.

No PowerShell:

```powershell
Copy-Item .env.example .env
```

Configure, no mínimo, as credenciais do PostgreSQL, uma chave secreta JWT forte e as credenciais da conta administradora:

| Variável | Finalidade |
|---|---|
| `DB_HOST`, `DB_PORT` | Host e porta do PostgreSQL. No Compose, o backend usa o serviço `postgres` e a porta interna `5432`; no host, a porta padrão publicada é `5433`. |
| `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Banco, usuário e senha do PostgreSQL. `DB_PASSWORD` é obrigatório no Compose. |
| `SPRING_PROFILES_ACTIVE` | Perfil Spring: `development` (padrão) ou `production`. |
| `JWT_SECRET`, `JWT_EXPIRATION` | Chave e duração, em milissegundos, dos tokens JWT. Defina uma chave forte e mantenha-a privada. |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Credenciais usadas para criar ou atualizar a conta administradora na inicialização do backend. São obrigatórias no Compose. |
| `CORS_ORIGINS` | Origens configuradas pelo Spring MVC, separadas por vírgula; o perfil de desenvolvimento fornece origens locais padrão. A lista do filtro CORS do Spring Security também está definida em `SecurityConfig.java` e precisa ser mantida compatível ao usar outras origens. |
| `VITE_API_URL` | URL da API usada pelo frontend. É incorporada ao build do Vite; no Compose, o padrão é `http://localhost:4000`. |
| `APP_PORT`, `BACKEND_PORT` | Portas publicadas para o frontend e o backend, respectivamente (`8080` e `4000` por padrão). |
| `DB_PORT` | Porta do PostgreSQL publicada na máquina (`5433` por padrão no Compose). |
| `GMAIL_ENABLED` e `GMAIL_*` | Ativam e configuram o envio de códigos por Gmail API/OAuth. Consulte [Configuração do Gmail](#configuração-do-gmail). |
| `TERMO_VERSAO` | Versão vigente do termo de uso/privacidade. |
| `EXCLUSAO_INTERVALO_MS` | Intervalo, em milissegundos, do agendador que processa solicitações vencidas de exclusão de conta. |
| `SHOW_SQL` | Habilita a exibição de SQL pelo Hibernate no perfil de desenvolvimento. |

O arquivo `.env` na raiz é usado pelo Docker Compose e também é importado pela configuração Spring. O perfil `production` exige que as variáveis de conexão e CORS sejam informadas explicitamente. Não use credenciais de exemplo em ambientes publicados.

## Execução com Docker Compose

1. Crie e preencha o `.env`, incluindo `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL` e `ADMIN_PASSWORD`.
2. Na raiz do projeto, construa e inicie os serviços:

   ```powershell
   docker compose up --build
   ```

3. Acesse:
   - Aplicação web: `http://localhost:8080`
   - API: `http://localhost:4000`
   - PostgreSQL na máquina host: `localhost:5433`

O backend aguarda a verificação de saúde do PostgreSQL antes de iniciar. Para parar os serviços, use `Ctrl+C` e depois:

```powershell
docker compose down
```

Os dados do PostgreSQL ficam no volume `postgres_data` e são mantidos ao parar/remover os contêineres sem remover o volume. Os scripts de `database/` são executados automaticamente pelo PostgreSQL somente na primeira inicialização de um volume vazio.

## Execução local para desenvolvimento

### 1. Inicie o PostgreSQL

É possível iniciar apenas o banco definido no Compose:

```powershell
docker compose up -d postgres
```

Preencha `DB_PASSWORD` no `.env` antes de iniciar o serviço. O Compose publica o PostgreSQL em `localhost:5433` por padrão e carrega os scripts SQL automaticamente se o volume ainda estiver vazio.

### 2. Inicie o backend

Em um terminal na pasta `backend`:

```powershell
mvn spring-boot:run
```

O perfil padrão é `development`. Para selecionar outro perfil, defina `SPRING_PROFILES_ACTIVE` no ambiente antes de iniciar o Maven. A API fica disponível em `http://localhost:4000`.

### 3. Inicie o frontend

Em outro terminal, na pasta `frontend`:

```powershell
npm install
$env:VITE_API_URL = "http://localhost:4000"
npm run dev
```

Use a URL exibida pelo Vite, normalmente `http://localhost:5173`. Se mudar a origem ou a porta, inclua-a em `CORS_ORIGINS` e na lista do filtro CORS em `backend/src/main/java/br/com/aulas/config/SecurityConfig.java`, depois reinicie o backend. A variável `VITE_API_URL` é lida pelo Vite durante a execução/build do frontend.

## Banco de dados

Os scripts em `database/` são aplicados em ordem alfabética pelo contêiner PostgreSQL:

| Script | Conteúdo |
|---|---|
| `01-log.sql` | Tabelas de log de acesso, códigos de verificação e auditoria. |
| `02-usuarios.sql` | Usuários e solicitações de alteração de dados cadastrais. |
| `03-termoprivacidade.sql` | Aceites dos termos. |
| `04-aulas.sql` | Aulas, temas e acompanhamento das aulas por aluno. |
| `05-simulados.sql` | Simulados, perguntas, opções e registros de realização. |
| `06-torneios.sql` | Turmas, equipes, torneios, inscrições e lutas. |
| `07-dashboard.sql` | Estruturas adicionais usadas pelo dashboard e pelas realizações de simulados. |
| `08-function_triggers.sql` | Funções e gatilhos do banco. |
| `09-insert.sql` | Dados iniciais, como temas e registros-base. |

O backend usa `ddl-auto: validate`: o esquema precisa existir e ser compatível com as entidades antes da aplicação iniciar. Em uma instalação existente, alterações nos arquivos SQL não são reaplicadas automaticamente pelo PostgreSQL; aplique migrações de forma controlada e faça backup antes de alterar dados persistidos. Os scripts de inicialização contêm comandos de criação/substituição de tabelas e devem ser usados apenas na inicialização apropriada do banco.

## Autenticação e autorização

- A API usa autenticação sem estado (stateless) com JWT. O frontend guarda os dados da sessão no armazenamento local do navegador e envia o token no cabeçalho `Authorization`.
- O fluxo de autenticação oferece endpoints para login, solicitação/verificação de código, recuperação de senha e logout. O envio de códigos por e-mail depende da configuração de Gmail.
- As rotas e operações são limitadas por perfil, incluindo `admin`, `professor` e `aluno`. O backend aplica as regras de segurança; ocultar uma página no frontend não substitui essa validação.
- O aceite do termo vigente é exigido para as operações protegidas, com exceção do acesso administrativo previsto pela aplicação.
- Senhas são armazenadas com hash BCrypt.
- Eventos relevantes e tentativas de acesso negadas são registrados pelos serviços de auditoria.

### Configuração do Gmail

Para envio de códigos por Gmail, configure `GMAIL_ENABLED=true`, `GMAIL_SENDER`, `GMAIL_CLIENT_ID`, `GMAIL_CLIENT_SECRET` e `GMAIL_REFRESH_TOKEN`. `GMAIL_CODE_EXPIRATION_SECONDS` controla a validade do código e `GMAIL_RESEND_COOLDOWN_SECONDS` o intervalo mínimo para solicitar outro código. Com o envio desabilitado ou credenciais inválidas, os fluxos que dependem do e-mail podem não ser concluídos.

## API

A API usa JSON e está disponível, por padrão, em `http://localhost:4000`. Os recursos principais estão agrupados nos seguintes caminhos:

| Caminho | Recursos |
|---|---|
| `/auth` | Login, códigos de verificação, recuperação de senha e logout. |
| `/aulas` | Aulas e temas; operações administrativas/de ensino. |
| `/simulados` | Gerenciamento de simulados. |
| `/api/usuarios` | Administração de usuários e análise de solicitações de alteração. |
| `/api/meus-dados` | Dados pessoais, solicitação de alteração, exportação, senha e exclusão de conta. |
| `/api/aulas-aluno`, `/api/simulados-aluno` | Acompanhamento e realização de atividades pelo aluno. |
| `/api/turmas`, `/api/equipes`, `/api/torneios` | Turmas, equipes e torneios. |
| `/api/dashboard`, `/api/auditoria` | Resumo do dashboard e registros de auditoria. |
| `/api/termo` | Aceite ou recusa do termo. |

Os endpoints públicos são limitados; os demais requerem autenticação e autorização conforme o endpoint e o perfil do usuário.

## Testes e build

Na pasta `backend`, execute os testes automatizados:

```powershell
mvn test
```

Para gerar o pacote executável do backend:

```powershell
mvn package
```

Na pasta `frontend`, gere a versão de produção:

```powershell
npm run build
```

O resultado do frontend é gerado em `frontend/dist/`. A imagem Docker do frontend serve essa versão pelo Nginx, com fallback para a SPA.

## Solução de problemas

- **Backend não inicia por erro de conexão**: confirme que o PostgreSQL está saudável, que `DB_HOST` e `DB_PORT` correspondem ao modo de execução e que a senha está correta.
- **Erro de validação do esquema JPA**: confirme que os scripts SQL foram aplicados ao banco esperado e que o volume já não contém um banco antigo com esquema incompatível.
- **Frontend não alcança a API**: confirme `VITE_API_URL`, a porta do backend e se a origem do frontend está autorizada tanto em `CORS_ORIGINS` quanto no filtro CORS do Spring Security; reinicie/reconstrua o frontend depois de alterar variáveis usadas pelo Vite.
- **Login ou recuperação não envia código**: confira se Gmail está habilitado e se as credenciais OAuth e o endereço remetente estão configurados.
- **Compose informa variável obrigatória ausente**: preencha no `.env` as variáveis requeridas pelo Compose, especialmente `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL` e `ADMIN_PASSWORD`.
