
CREATE TABLE aulastemas(
	IdAulaTema SERIAL PRIMARY KEY,
	Descricao  VARCHAR(120) NOT NULL
);

CREATE TABLE aulas(
	IdAula       SERIAL PRIMARY KEY,
	IdAulaTema   INTEGER NOT NULL,
	Titulo       VARCHAR(180) NOT NULL,
	Conteudo     TEXT NOT NULL,
	DtRegistro   TIMESTAMP,
	UsrRegistro  VARCHAR(2000),
	DtAlteracao  TIMESTAMP,
	UsrAlteracao VARCHAR(2000),
	CONSTRAINT fk_idaulatema FOREIGN KEY (IdAulaTema) REFERENCES aulastemas (IdAulaTema)
);

CREATE TABLE aulasaluno(
	IdAulaAluno  BIGSERIAL PRIMARY KEY,
    IdAula       BIGINT NOT NULL,
    IdAulaTema   INTEGER NOT NULL,
	IdUsuario    BIGINT NOT NULL,
	Concluida    VARCHAR(1) NOT NULL DEFAULT 'N',
	DtRegistro   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_idaula_aluno FOREIGN KEY (IdAula) REFERENCES aulas (IdAula),
	CONSTRAINT fk_idusuario_aluno FOREIGN KEY (IdUsuario) REFERENCES usuarios (IdUsuario)
);