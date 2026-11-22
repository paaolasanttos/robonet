CREATE TABLE usuarios(
	IdUsuario    BIGSERIAL PRIMARY KEY,
	Nome         VARCHAR(2000) NOT NULL,
	email        VARCHAR(2000) NOT NULL,
    senha        TEXT,
	ra           VARCHAR(50),
	Perfil       VARCHAR(50) NOT NULL,
	Status       VARCHAR(30) NOT NULL DEFAULT 'Ativo',
	AcessoMFV    VARCHAR(1) NOT NULL DEFAULT 'N',
	DtRegistro   TIMESTAMP,
	UsrRegistro  VARCHAR(2000),
	DtAlteracao  TIMESTAMP,
	UsrAlteracao VARCHAR(2000)	
);
CREATE UNIQUE INDEX ux_usuarios_email ON usuarios (LOWER(TRIM(email))) WHERE Status = 'Ativo';

CREATE TABLE usuariosperfilacessos(
	IdUsuarioPerfilAcesso BIGSERIAL PRIMARY KEY,
	Perfil                VARCHAR(50) NOT NULL,
	Menu                  VARCHAR(80) NOT NULL
);
CREATE UNIQUE INDEX ux_usuariosperfilacessos_perfil ON usuariosperfilacessos (Perfil, Menu);

CREATE TABLE solicitacoesalteracoes(
	IdSolicitacaoAlteracao BIGSERIAL PRIMARY KEY,
	IdUsuario              BIGSERIAL NOT NULL,
	Nome         		   VARCHAR(2000) NOT NULL,
	email        		   VARCHAR(2000) NOT NULL,
	ra           		   VARCHAR(50),
	Perfil       		   VARCHAR(50) NOT NULL,
	Turma        		   VARCHAR(2000),
	Status       		   VARCHAR(30) NOT NULL DEFAULT 'Pendente',
	Motivo      		   TEXT,
	DtRegistro   		   TIMESTAMP,
	UsrRegistro  		   VARCHAR(2000),
	DtAnalise    		   TIMESTAMP,
	UsrAnalise   		   VARCHAR(2000)	
);

CREATE TABLE solicitacoesexclusao(
	IdSolicitacaoExclusao BIGSERIAL PRIMARY KEY,
	IdUsuario             BIGINT NOT NULL,
	Email                 VARCHAR(2000) NOT NULL,
	Status                VARCHAR(20) NOT NULL DEFAULT 'Pendente',
	Ip                    VARCHAR(100),
	DtSolicitacao         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	DtLimite              TIMESTAMP NOT NULL,
	DtConclusao           TIMESTAMP,
	UsrAnalise            VARCHAR(255),
	CONSTRAINT fk_solicitacoesexclusao_idusuario FOREIGN KEY (IdUsuario) REFERENCES usuarios (IdUsuario)
);
CREATE UNIQUE INDEX ux_solicitacoesexclusao_pendente ON solicitacoesexclusao (IdUsuario) WHERE Status = 'Pendente';

CREATE TABLE solicitacoescadastro(
    IdSolicitacaoCadastro BIGSERIAL PRIMARY KEY,
    Nome                  VARCHAR(50) NOT NULL,
    Email                 VARCHAR(255) NOT NULL,
    RA                    VARCHAR(11),
    Perfil                VARCHAR(20) NOT NULL,
    Senha                 VARCHAR(100),
    Status                VARCHAR(20) NOT NULL DEFAULT 'Pendente',
    Motivo                VARCHAR(2000),
    Ip                    VARCHAR(100),
    DtSolicitacao         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    DtAnalise             TIMESTAMP,
    UsrAnalise            VARCHAR(255)
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_solicitacoescadastro_pendente ON solicitacoescadastro (LOWER(Email)) WHERE Status = 'Pendente';

CREATE TABLE gmailtokens (
    id             BIGINT PRIMARY KEY,
    refreshtoken   VARCHAR(2048) NOT NULL,
    dtatualizacao  TIMESTAMP NOT NULL
);