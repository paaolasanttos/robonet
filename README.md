# 🤖 RoboNet

### Plataforma Web para Gerenciamento de Torneios de Robótica Educacional

A **RoboNet** é uma plataforma web desenvolvida para auxiliar no gerenciamento e organização de torneios de robótica educacional.

O projeto busca centralizar informações relacionadas a **usuários, equipes, participantes, torneios, partidas e resultados**, proporcionando uma solução mais organizada, segura e eficiente para os responsáveis pela realização das competições.

O projeto é desenvolvido como Trabalho de Conclusão do curso de **Bacharelado em Sistemas de Informação da Universidade de Mogi das Cruzes (UMC)**.

---

## 📌 Sobre o projeto

As competições de robótica educacional envolvem diversos processos de organização, incluindo o gerenciamento de equipes, participantes, torneios, partidas, resultados e classificações.

A RoboNet surge da necessidade de uma solução integrada para esse contexto. O projeto foi motivado pela experiência com a equipe **Robótica Sempre Mais (RSM)**, de Mogi das Cruzes, que atualmente não possui um sistema integrado para o gerenciamento de torneios, equipes e participantes.

A plataforma tem como proposta centralizar essas informações e disponibilizar os recursos de acordo com os diferentes perfis e permissões dos usuários.

---

## 🎯 Objetivo

O objetivo geral do projeto é:

> Desenvolver uma plataforma web para gerenciar torneios de robótica educacional, centralizando usuários, equipes, torneios, partidas e resultados, com controle de acesso, auditoria e mecanismos de privacidade e proteção de dados alinhados aos princípios da LGPD.

---

## ✨ Funcionalidades

A versão proposta da RoboNet contempla as seguintes funcionalidades:

* 👤 Cadastro e autenticação de usuários
* 🔐 Controle de acesso baseado em perfis e permissões
* 🤖 Cadastro e gerenciamento de equipes
* 👥 Gerenciamento de participantes autorizados
* 🏆 Criação e configuração de torneios
* ⚔️ Organização e gerenciamento de partidas
* 📊 Registro e consulta de resultados
* 📈 Painéis de acompanhamento
* 📝 Registro de ações para auditoria
* 🔒 Proteção de dados pessoais
* 🛡️ Práticas de segurança da informação
* 🗑️ Mecanismos de retenção e exclusão de dados quando aplicáveis
* 📅 Integração com Google Calendar
* 📧 Envio de mensagens e notificações por e-mail

---

## 🏗️ Arquitetura

A aplicação segue uma arquitetura baseada no padrão **MVC (Model-View-Controller)**, separando as responsabilidades entre apresentação, regras de negócio e persistência de dados.

### Fluxo simplificado

```text
┌─────────────────────┐
│      Usuário        │
│ Navegador Web       │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│      Front-end      │
│ React / HTML / CSS  │
│      JavaScript     │
└──────────┬──────────┘
           │
           │ API REST
           ▼
┌─────────────────────┐
│       Back-end      │
│   Java + Spring     │
│       Boot          │
│                     │
│ Controller          │
│ Service             │
│ Repository          │
└──────┬─────────┬────┘
       │         │
       │         ├─────────────────┐
       │         │                 │
       ▼         ▼                 ▼
┌───────────┐ ┌──────────────┐ ┌─────────────┐
│ PostgreSQL│ │ Google       │ │   Serviço   │
│           │ │ Calendar API │ │  de E-mail  │
└───────────┘ └──────────────┘ └─────────────┘
```

A arquitetura proposta utiliza **React, HTML, CSS e JavaScript** no front-end, enquanto o back-end será desenvolvido em **Java com Spring Boot**, utilizando uma API REST para comunicação entre as camadas. O armazenamento dos dados será realizado utilizando **PostgreSQL**.

---

## 🛠️ Tecnologias

### Front-end

* **React**
* **JavaScript**
* **HTML5**
* **CSS3**

Responsável pela interface e interação dos usuários com a plataforma.

### Back-end

* **Java**
* **Spring Boot**
* **API REST**

Responsável pelas regras de negócio, autenticação, autorização, validação e comunicação com o banco de dados.

### Banco de dados

* **PostgreSQL**

Responsável pelo armazenamento estruturado das informações da plataforma, incluindo usuários, equipes, torneios, partidas e resultados.

### Integrações

* **Google Calendar API**
* **Serviço de e-mail**

As integrações têm como objetivo auxiliar no gerenciamento de eventos e no envio de mensagens e notificações aos usuários.

---

## 🔐 Segurança e privacidade

A segurança e a proteção de dados são consideradas requisitos importantes da RoboNet.

A plataforma prevê:

* Autenticação de usuários;
* Autorização baseada em perfis e permissões;
* Controle de acesso às informações;
* Proteção de credenciais;
* Registro de ações relevantes para auditoria;
* Minimização da coleta de dados;
* Controle de acesso a informações pessoais;
* Mecanismos de retenção e exclusão de dados;
* Possibilidade de anonimização quando aplicável.

Essas medidas são planejadas de forma alinhada aos princípios da **Lei Geral de Proteção de Dados Pessoais (LGPD)**.

---

## 👥 Público-alvo

A plataforma é destinada principalmente a:

* Organizadores de torneios;
* Administradores;
* Equipes de robótica;
* Técnicos;
* Responsáveis pelas equipes;
* Participantes autorizados.

---

## 📂 Estrutura do projeto

A estrutura poderá ser organizada de acordo com a separação entre front-end e back-end:

```text
RoboNet/
│
├── frontend/
│   ├── src/
│   └── package.json
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   └── pom.xml
│
│
└── README.md
```

> A estrutura acima representa uma organização sugerida e pode ser ajustada conforme a implementação do projeto.

---

## 🚀 Como executar o projeto

### Arquitetura de deploy

| Camada | Serviço | Pasta |
|---|---|---|
| Front-end (React + Vite) | **Vercel** | `frontend/` |
| Back-end (Spring Boot) | **Render** (Docker, plano Free) | `backend/` |
| Banco de dados (PostgreSQL) | **Supabase** (Session pooler) | scripts em `database/` |

### Variáveis de ambiente

As variáveis seguem o padrão do Spring Boot: o nome da propriedade em maiúsculas, com `.` e `-` trocados por `_`
(ex.: `spring.datasource.url` → `SPRING_DATASOURCE_URL`). A lista completa está em `.env.example`.

| Variável | Onde | Descrição |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Render | `production` (local: `development`) |
| `SPRING_DATASOURCE_URL` | Render | `jdbc:postgresql://aws-0-us-east-2.pooler.supabase.com:5432/postgres?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Render | usuário do pooler (`postgres.<id-do-projeto>`) |
| `SPRING_DATASOURCE_PASSWORD` | Render | senha do banco no Supabase |
| `APP_CORS_ORIGINS` | Render | URL do front na Vercel, sem barra no final |
| `SECURITY_JWT_SECRET` | Render | chave longa e aleatória |
| `SECURITY_ADMIN_EMAIL` / `SECURITY_ADMIN_PASSWORD` | Render | administrador criado na inicialização |
| `GMAIL_*` | Render | envio de e-mail (opcional, `GMAIL_ENABLED=false` desliga) |
| `VITE_API_URL` | Vercel | URL do back-end no Render |

> `PORT` é definida automaticamente pelo Render. Nunca versione o arquivo `.env`.

### 1. Banco de dados — Supabase

1. No Supabase, clique em **Connect** e copie os dados do **Session pooler** (porta 5432).
   Não use a *Direct connection*: ela usa IPv6, e o Render não se conecta por IPv6.
2. Crie o schema executando os scripts da pasta `database` em ordem numérica no **SQL Editor**.
   O back-end usa `ddl-auto: validate`, então as tabelas precisam existir antes de ele subir.

### 2. Back-end — Render

**Opção A (Blueprint):** no Render, **New → Blueprint**, escolha este repositório. O arquivo `render.yaml`
cria o serviço `robonet-api` e pede os valores das variáveis.

**Opção B (manual):** **New → Web Service**, com:

- **Root Directory:** `backend`
- **Language:** Docker
- **Region:** Ohio (mesma região do pooler do Supabase)
- **Instance Type:** Free
- **Health Check Path:** `/health`
- **Environment Variables:** as da tabela acima

Ao terminar, teste `https://<seu-servico>.onrender.com/health`. A resposta deve ser `{"status":"UP"}`.

> No plano gratuito, o serviço desliga após 15 minutos sem uso, e o primeiro acesso seguinte pode levar até 1 minuto.
> Um monitor gratuito (ex.: UptimeRobot) chamando `/health` a cada 10 minutos evita isso.

### 3. Front-end — Vercel

1. **Add New → Project**, escolha este repositório.
2. **Root Directory:** `frontend` (o framework Vite é detectado pelo `vercel.json`).
3. **Environment Variables:** `VITE_API_URL` = URL do Render (ex.: `https://robonet-api.onrender.com`).
4. Deploy. Sempre que mudar `VITE_API_URL`, faça **Redeploy**, porque ela é aplicada no build.
5. Copie a URL da Vercel e coloque em `APP_CORS_ORIGINS` no Render.

### Execução local

```bash
cp .env.example .env     # preencha senha do banco, JWT e admin
docker compose up --build
```

Front-end em `http://localhost:8080` e back-end em `http://localhost:4000`.

Sem Docker: `cd backend && mvn spring-boot:run` e `cd frontend && npm install && npm run dev`.

---

## 📝 Alterações recentes

- Adicionada opção para controlar o acesso por MFV.
- Criada a tabela de solicitações de alteração dos dados pelo usuario o qual ficará sob analise do administrador aceitar ou não.
- Criada opção para o Administrador analisar as solicitações de alteração dos dados dos usuários.
- Alterada a application properties para application profile.
- Ajustada a configuração das variáveis de ambiente.
- Remodelada a visualização do "Meus Dados", "Aulas", "Simulados" para uma melhor experiência de usuário.
- Desenvolvido o Dashboard

---

## 🗄️ Banco de dados

O projeto utiliza **PostgreSQL** para persistência dos dados.

As configurações de conexão com o banco de dados devem ser definidas nas configurações do back-end e **não devem conter credenciais diretamente no código-fonte ou no repositório**.

---

## 📅 Integrações externas

### Google Calendar

A integração com o Google Calendar tem como finalidade auxiliar no gerenciamento de eventos e compromissos relacionados aos torneios.

### E-mail

O serviço de e-mail poderá ser utilizado para o envio de mensagens e notificações aos usuários da plataforma.

---

## 📚 Contexto acadêmico

Este projeto está sendo desenvolvido como Trabalho de Finalização do curso de:

**Bacharelado em Sistemas de Informação**
**Universidade de Mogi das Cruzes — UMC**
**Mogi das Cruzes — SP**
**2026**

### Autores

* **Paola dos Santos Ramos**
* **Bruno Bernardelli Fraudio**

---

## 🔮 Possíveis evoluções

Entre as possibilidades de evolução da plataforma estão:

* Geração de relatórios;
* Integração com outras plataformas;
* Suporte a diferentes modalidades de competição;
* Ampliação dos mecanismos de análise dos torneios;
* Novos recursos de acompanhamento e gerenciamento.

---

## 📖 Referências

O projeto utiliza como referências materiais relacionados a Java, React, PostgreSQL e gerenciamento de equipes e competições de robótica.

Entre as referências utilizadas na documentação estão:

* Oracle — Java Documentation
* Meta — React Documentation
* PostgreSQL Global Development Group — PostgreSQL Documentation
* FTC TeamForge
* Pitside
* RoboCup Brasil — Olimpo

---


## 📄 Licença

Este projeto foi desenvolvido para fins **acadêmicos** como parte do Trabalho de Finalização do curso de Bacharelado em Sistemas de Informação.

A definição de uma licença específica para distribuição e utilização do software poderá ser realizada posteriormente.
