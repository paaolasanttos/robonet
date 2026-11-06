CREATE TABLE IF NOT EXISTS Equipes(
    IdEquipe   BIGSERIAL PRIMARY KEY,
    Nome       VARCHAR(120) NOT NULL,
    IdTurma    BIGINT NOT NULL,
    DtRegistro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipes_idturma FOREIGN KEY (IdTurma) REFERENCES Turmas (IdTurma)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_equipes_nome
    ON Equipes (IdTurma, LOWER(TRIM(Nome)));

CREATE TABLE IF NOT EXISTS EquipesMembros(
    IdEquipeMembro BIGSERIAL PRIMARY KEY,
    IdEquipe       BIGINT NOT NULL,
    IdUsuario      BIGINT NOT NULL,
    CONSTRAINT fk_membros_idequipe FOREIGN KEY (IdEquipe) REFERENCES Equipes (IdEquipe) ON DELETE CASCADE,
    CONSTRAINT fk_membros_idusuario FOREIGN KEY (IdUsuario) REFERENCES Usuarios (IdUsuario),
    CONSTRAINT ux_membros_idusuario UNIQUE (IdUsuario)
);

CREATE TABLE IF NOT EXISTS Torneios(
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

CREATE TABLE IF NOT EXISTS TorneiosEquipes(
    IdTorneioEquipe BIGSERIAL PRIMARY KEY,
    IdTorneio       BIGINT NOT NULL,
    IdEquipe        BIGINT NOT NULL,
    CONSTRAINT fk_torneiosequipes_idtorneio FOREIGN KEY (IdTorneio) REFERENCES Torneios (IdTorneio) ON DELETE CASCADE,
    CONSTRAINT fk_torneiosequipes_idequipe FOREIGN KEY (IdEquipe) REFERENCES Equipes (IdEquipe),
    CONSTRAINT ux_torneiosequipes UNIQUE (IdTorneio, IdEquipe)
);

CREATE TABLE IF NOT EXISTS Lutas(
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