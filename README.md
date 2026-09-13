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

### Pré-requisitos

Antes de executar a aplicação, é necessário possuir instalado:

* **Java JDK 26**
* **Node.js**
* **npm**
* **PostgreSQL**
* **Git**

### 1. Clonar o repositório

```bash
git clone https://github.com/SEU-USUARIO/robonet.git
```

Entre na pasta:

```bash
cd robonet
```

### 2. Executar o Back-end

Entre na pasta do back-end:

```bash
cd backend
```

Execute o projeto utilizando o Maven:

```bash
./mvnw spring-boot:run
```

No Windows, caso necessário:

```bash
mvnw.cmd spring-boot:run
```

### 3. Executar o Front-end

Em outro terminal:

```bash
cd frontend
```

Instale as dependências:

```bash
npm install
```

Execute a aplicação:

```bash
npm start
```

A aplicação ficará disponível no endereço configurado pelo projeto.

---

## 🗄️ Banco de dados

O projeto utiliza **PostgreSQL** para persistência dos dados.

As principais entidades previstas incluem:

```text
Usuários
   │
   ├── Perfis e Permissões
   │
   └── Auditoria

Equipes
   │
   └── Participantes

Torneios
   │
   ├── Partidas
   │
   └── Resultados
```

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
