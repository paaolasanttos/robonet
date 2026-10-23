--DROP TABLE será executado durante o desenvolvimento do sistema, apos isso, iremos remover

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
	IdUsuario   BIGSERIAL PRIMARY KEY,
	Nome        VARCHAR(2000) NOT NULL,
	email       VARCHAR(2000) NOT NULL,
    senha       TEXT,
	rgm         VARCHAR(50),
	Perfil      VARCHAR(50) NOT NULL,
	Status      VARCHAR(30) NOT NULL DEFAULT 'Ativo',
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
    IdUsuario     BIGINT NOT NULL,
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
	Descricao  VARCHAR(120) NOT NULL
);

CREATE TABLE Aulas(
	IdAula       SERIAL PRIMARY KEY,
	IdAulaTema   INTEGER NOT NULL,
	Titulo       VARCHAR(180) NOT NULL,
	Conteudo     TEXT NOT NULL,
	DtRegistro   TIMESTAMP,
	UsrRegistro  VARCHAR(50),
	DtAlteracao  TIMESTAMP,
	UsrAlteracao VARCHAR(50),
	CONSTRAINT fk_idaulatema FOREIGN KEY (IdAulaTema) REFERENCES AulasTemas (IdAulaTema)
);

CREATE TABLE AulasAluno(
	IdAulaAluno  BIGSERIAL PRIMARY KEY,
    IdAula       BIGINT NOT NULL,
    IdAulaTema   INTEGER NOT NULL,
	IdUsuario    BIGINT NOT NULL,
	Concluida    VARCHAR(1) NOT NULL DEFAULT 'N',
	DtRegistro   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_idaula_aluno FOREIGN KEY (IdAula) REFERENCES Aulas (IdAula),
	CONSTRAINT fk_idusuario_aluno FOREIGN KEY (IdUsuario) REFERENCES Usuarios (IdUsuario)
);

CREATE OR REPLACE FUNCTION AulasInsertBefore()
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
EXECUTE FUNCTION AulasInsertBefore();

CREATE OR REPLACE FUNCTION AulasInsertAfter()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO AulasAluno (IdAula, IdAulaTema, IdUsuario, Concluida)
    SELECT NEW.IdAula, NEW.IdAulaTema, u.IdUsuario, 'N'
      FROM Usuarios u
     WHERE u.Status = 'Ativo'
       AND u.Perfil = 'aluno';
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER AulasInsertAfter
AFTER INSERT
ON Aulas
FOR EACH ROW
EXECUTE FUNCTION AulasInsertAfter();

CREATE OR REPLACE FUNCTION AulasUpdateBefore()
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
EXECUTE FUNCTION AulasUpdateBefore();

CREATE OR REPLACE FUNCTION AulasDeleteBefore()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM AulasAluno WHERE IdAula = OLD.IdAula;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER AulasDelete
BEFORE DELETE
ON Aulas
FOR EACH ROW
EXECUTE FUNCTION AulasDeleteBefore();

DROP TABLE IF EXISTS Lutas;
DROP TABLE IF EXISTS TorneiosEquipes;
DROP TABLE IF EXISTS Torneios;
DROP TABLE IF EXISTS EquipesMembros;
DROP TABLE IF EXISTS Equipes;
DROP TABLE IF EXISTS UsuariosTurmas;
DROP TABLE IF EXISTS Turmas;

CREATE TABLE Turmas(
    IdTurma    BIGSERIAL PRIMARY KEY,
    Nome       VARCHAR(120) NOT NULL,
    DtRegistro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX ux_turmas_nome ON Turmas (LOWER(TRIM(Nome)));

CREATE TABLE UsuariosTurmas(
    IdUsuarioTurma BIGSERIAL PRIMARY KEY,
    IdUsuario      BIGINT NOT NULL,
    IdTurma        BIGINT NOT NULL,
    CONSTRAINT fk_usuariosturmas_idusuario FOREIGN KEY (IdUsuario) REFERENCES Usuarios (IdUsuario),
    CONSTRAINT fk_usuariosturmas_idturma FOREIGN KEY (IdTurma) REFERENCES Turmas (IdTurma),
    CONSTRAINT ux_usuariosturmas UNIQUE (IdUsuario, IdTurma)
);

CREATE TABLE Equipes(
    IdEquipe   BIGSERIAL PRIMARY KEY,
    Nome       VARCHAR(120) NOT NULL,
    IdTurma    BIGINT NOT NULL,
    DtRegistro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipes_idturma FOREIGN KEY (IdTurma) REFERENCES Turmas (IdTurma)
);

CREATE UNIQUE INDEX ux_equipes_nome ON Equipes (IdTurma, LOWER(TRIM(Nome)));

CREATE TABLE EquipesMembros(
    IdEquipeMembro BIGSERIAL PRIMARY KEY,
    IdEquipe       BIGINT NOT NULL,
    IdUsuario      BIGINT NOT NULL,
    CONSTRAINT fk_membros_idequipe FOREIGN KEY (IdEquipe) REFERENCES Equipes (IdEquipe) ON DELETE CASCADE,
    CONSTRAINT fk_membros_idusuario FOREIGN KEY (IdUsuario) REFERENCES Usuarios (IdUsuario),
    CONSTRAINT ux_membros_idusuario UNIQUE (IdUsuario)
);

CREATE TABLE Torneios(
    IdTorneio   BIGSERIAL PRIMARY KEY,
    Nome        VARCHAR(180) NOT NULL,
    Local       VARCHAR(180),
    DtInicio    DATE NOT NULL,
    DtFim       DATE,
    Regras      TEXT,
    Status      VARCHAR(30) NOT NULL DEFAULT 'Aberto',
    IdProfessor BIGINT NOT NULL,
    DtRegistro  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_torneios_idprofessor FOREIGN KEY (IdProfessor) REFERENCES Usuarios (IdUsuario)
);

CREATE TABLE TorneiosEquipes(
    IdTorneioEquipe BIGSERIAL PRIMARY KEY,
    IdTorneio       BIGINT NOT NULL,
    IdEquipe        BIGINT NOT NULL,
    CONSTRAINT fk_torneiosequipes_idtorneio FOREIGN KEY (IdTorneio) REFERENCES Torneios (IdTorneio) ON DELETE CASCADE,
    CONSTRAINT fk_torneiosequipes_idequipe FOREIGN KEY (IdEquipe) REFERENCES Equipes (IdEquipe),
    CONSTRAINT ux_torneiosequipes UNIQUE (IdTorneio, IdEquipe)
);

CREATE TABLE Lutas(
    IdLuta      BIGSERIAL PRIMARY KEY,
    IdTorneio   BIGINT NOT NULL,
    Fase        INTEGER NOT NULL,
    Ordem       INTEGER NOT NULL,
    IdEquipeA   BIGINT,
    IdEquipeB   BIGINT,
    IdVencedor  BIGINT,
    DtResultado TIMESTAMP,
    CONSTRAINT fk_lutas_idtorneio FOREIGN KEY (IdTorneio) REFERENCES Torneios (IdTorneio) ON DELETE CASCADE,
    CONSTRAINT fk_lutas_idequipea FOREIGN KEY (IdEquipeA) REFERENCES Equipes (IdEquipe),
    CONSTRAINT fk_lutas_idequipeb FOREIGN KEY (IdEquipeB) REFERENCES Equipes (IdEquipe),
    CONSTRAINT fk_lutas_idvencedor FOREIGN KEY (IdVencedor) REFERENCES Equipes (IdEquipe),
    CONSTRAINT ux_lutas_posicao UNIQUE (IdTorneio, Fase, Ordem)
);
