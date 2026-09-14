# Robonet - Administração de usuários

Regra de negócio "Administração de usuários" completa: front-end em React,
back-end em Java/Spring Boot e banco PostgreSQL rodando no Docker.

## Estrutura

```
robonet-admin/
├── docker-compose.yml   -> sobe o PostgreSQL
├── backend/             -> Java + Spring Boot
└── frontend/            -> React + TypeScript + Vite
```

## Como rodar (na ordem)

Precisa ter instalado: Docker Desktop, Java 17+, Maven (ou usar o mvnw
que já vem no projeto) e Node.js.

### 1) Banco de dados (Docker)

Na raiz do projeto, sobe o container do PostgreSQL:

```
docker compose up -d
```

Isso baixa a imagem do Postgres (só na primeira vez), cria um container
chamado `robonet-postgres` e deixa o banco disponível na porta 5432 com
usuário `postgres`, senha `postgres` e um banco chamado `robonet` já
criado.

Pra verificar se subiu:

```
docker ps
```

Deve aparecer o container `robonet-postgres` rodando.

Pra parar (sem apagar os dados):

```
docker compose stop
```

Pra parar e apagar tudo (inclusive os dados salvos):

```
docker compose down -v
```

### 2) Back-end

Dentro da pasta `backend/`:

```
./mvnw spring-boot:run
```

Ou pelo IntelliJ/Eclipse: abre como projeto Maven e roda a classe
`AdminApplication`.

O back sobe em http://localhost:8080. As tabelas são criadas
automaticamente pelo Hibernate na primeira execução.

Testar se subiu: abre no navegador http://localhost:8080/api/usuarios
(vai voltar `[]` porque ainda não tem ninguém cadastrado).

### 3) Front-end

Em outro terminal, dentro de `frontend/`:

```
npm install
npm run dev
```

Abre em http://localhost:5173.

## O que dá pra fazer na tela

- Listar todos os usuários cadastrados
- Criar usuário novo (aluno, professor ou admin)
- Mudar o perfil de qualquer usuário (o select da coluna "Perfil")
- Ativar/desativar
- Excluir

## Rotas da API

| Método | Rota | O que faz |
|---|---|---|
| GET | /api/usuarios | Lista todos |
| POST | /api/usuarios | Cria um novo |
| PUT | /api/usuarios/{id}/perfil | Muda o perfil |
| PATCH | /api/usuarios/{id}/ativo | Ativa/desativa |
| DELETE | /api/usuarios/{id} | Exclui |
