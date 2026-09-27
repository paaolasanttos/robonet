--log
DROP TABLE IF EXISTS LogAcessos;

CREATE TABLE LogAcessos(
    IdLogAcesso BIGSERIAL PRIMARY KEY,
    Email       VARCHAR(2000) NOT NULL,
    DtAcesso    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS LogCodigoVerificacao;

CREATE TABLE LogCodigoVerificacao(
    IdLogCodigoVerificacao BIGSERIAL PRIMARY KEY,
    Email                  VARCHAR(2000) NOT NULL,
    Codigo                 VARCHAR(20) NOT NULL,
    Tipo                   VARCHAR(50) NOT NULL,
    DtGeracao              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS LogAuditoria;

CREATE TABLE LogAuditoria(
    IdLogAuditoria BIGSERIAL PRIMARY KEY,
    Email          VARCHAR(2000),
    Categoria      VARCHAR(50) NOT NULL,
    Acao           VARCHAR(100) NOT NULL,
    Descricao      TEXT,
    Ip             VARCHAR(100),
    Sucesso        BOOLEAN NOT NULL DEFAULT TRUE,
    DtRegistro     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ix_logauditoria_dtregistro ON LogAuditoria (DtRegistro DESC);

--usuarios
DROP TABLE IF EXISTS TermosAceites;
DROP TABLE IF EXISTS Usuarios;

CREATE TABLE Usuarios(
	IdUsuario   SERIAL PRIMARY KEY,
	Nome        VARCHAR(2000) NOT NULL,
	email       VARCHAR(2000) NOT NULL,
    senha       TEXT,
	rgm         VARCHAR(50),
	Perfil      VARCHAR(50) NOT NULL,
	Status      VARCHAR(30) NOT NULL DEFAULT 'Ativo',
	TermoAceite BOOLEAN NOT NULL DEFAULT FALSE,
	DtRegistro  TIMESTAMP,
	UsrRegistro VARCHAR(50),
	DtAlteracao  TIMESTAMP,
	UsrAlteracao VARCHAR(50)	
);

CREATE UNIQUE INDEX ux_usuarios_email ON Usuarios (LOWER(TRIM(email))) WHERE Status = 'Ativo';

CREATE OR REPLACE FUNCTION usuariosinsertbefore()
RETURNS TRIGGER AS $$
BEGIN
    NEW.DtRegistro := CURRENT_TIMESTAMP;
    NEW.UsrRegistro := COALESCE(NULLIF(CURRENT_SETTING('app.usuario', true), ''),CURRENT_USER);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER UsuariosInsert
BEFORE INSERT
ON Usuarios
FOR EACH ROW
EXECUTE FUNCTION usuariosinsertbefore();

CREATE OR REPLACE FUNCTION usuariosupdatebefore()
RETURNS TRIGGER AS $$
BEGIN
    NEW.DtAlteracao := CURRENT_TIMESTAMP;
    NEW.UsrAlteracao := COALESCE(NULLIF(CURRENT_SETTING('app.usuario', true), ''),CURRENT_USER);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER UsuariosUpdate
BEFORE UPDATE
ON Usuarios
FOR EACH ROW
EXECUTE FUNCTION usuariosupdatebefore();

--termo de uso e privacidade
CREATE TABLE TermosAceites(
    IdTermoAceite BIGSERIAL PRIMARY KEY,
    IdUsuario     INT NOT NULL,
    Versao        VARCHAR(20) NOT NULL,
    Ip            VARCHAR(100),
    DtAceite      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_termo_idusuario FOREIGN KEY (IdUsuario) REFERENCES Usuarios (IdUsuario)
);

--aulas
DROP TABLE IF EXISTS Aulas;
DROP TABLE IF EXISTS AulasTemas;

CREATE TABLE AulasTemas(
	IdAulaTema SERIAL PRIMARY KEY,
	Descricao  VARCHAR(255) NOT NULL
);

CREATE TABLE Aulas(
	IdAula       SERIAL PRIMARY KEY,
	IdAulaTema   INT NOT NULL,
	Titulo       VARCHAR(2000) NOT NULL,
	Conteudo     TEXT NOT NULL,
	DtRegistro   TIMESTAMP,
	UsrRegistro  VARCHAR(50),
	DtAlteracao  TIMESTAMP,
	UsrAlteracao VARCHAR(50),
	CONSTRAINT fk_idaulatema FOREIGN KEY (IdAulaTema) REFERENCES AulasTemas (IdAulaTema)
);

CREATE OR REPLACE FUNCTION aulasinsertbefore()
RETURNS TRIGGER AS $$
BEGIN
    NEW.DtRegistro := CURRENT_TIMESTAMP;
    NEW.UsrRegistro := COALESCE(NULLIF(CURRENT_SETTING('app.usuario', true), ''),CURRENT_USER);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER AulasInsert
BEFORE INSERT
ON Aulas
FOR EACH ROW
EXECUTE FUNCTION aulasinsertbefore();

CREATE OR REPLACE FUNCTION aulasupdatebefore()
RETURNS TRIGGER AS $$
BEGIN
    NEW.DtAlteracao := CURRENT_TIMESTAMP;
    NEW.UsrAlteracao := COALESCE(NULLIF(CURRENT_SETTING('app.usuario', true), ''),CURRENT_USER);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER AulasUpdate
BEFORE UPDATE
ON Aulas
FOR EACH ROW
EXECUTE FUNCTION aulasupdatebefore();
