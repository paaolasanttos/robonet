DROP TABLE IF EXISTS Usuarios;

CREATE TABLE Usuarios(
	IdUsuario    BIGSERIAL PRIMARY KEY,
	Nome         VARCHAR(2000) NOT NULL,
	email        VARCHAR(2000) NOT NULL,
    senha        TEXT,
	rgm          VARCHAR(50),
	Perfil       VARCHAR(50) NOT NULL,
	Status       VARCHAR(30) NOT NULL DEFAULT 'Ativo',
	AcessoMFV    VARCHAR(1) NOT NULL DEFAULT 'N',
	DtRegistro   TIMESTAMP,
	UsrRegistro  VARCHAR(2000),
	DtAlteracao  TIMESTAMP,
	UsrAlteracao VARCHAR(2000)	
);
CREATE UNIQUE INDEX ux_usuarios_email ON Usuarios (LOWER(TRIM(email))) WHERE Status = 'Ativo';

DROP TABLE IF EXISTS UsuariosSolicitacoes;

CREATE TABLE UsuariosSolicitacoes(
	IdUsuario    BIGSERIAL PRIMARY KEY,
	Nome         VARCHAR(2000) NOT NULL,
	email        VARCHAR(2000) NOT NULL,
	rgm          VARCHAR(50),
	Perfil       VARCHAR(50) NOT NULL,
	Turma        VARCHAR(2000),
	Status       VARCHAR(30) NOT NULL DEFAULT 'Pendente de analise',
	Motivo       TEXT,
	DtRegistro   TIMESTAMP,
	UsrRegistro  VARCHAR(2000),
	DtAnalise    TIMESTAMP,
	UsrAnalise   VARCHAR(2000)	
);