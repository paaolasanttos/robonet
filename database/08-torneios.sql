CREATE TABLE torneios(
    IdTorneio   BIGSERIAL PRIMARY KEY,
    Nome        VARCHAR(180) NOT NULL,
    Local       VARCHAR(180),
    DtInicio    DATE NOT NULL,
    DtFim       DATE,
    Regras      TEXT,
    Status      VARCHAR(30) NOT NULL DEFAULT 'Aberto',
    IdProfessor BIGINT NOT NULL,
    DtRegistro  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_torneios_idprofessor FOREIGN KEY (IdProfessor) REFERENCES usuarios (IdUsuario)
);

CREATE TABLE torneiosequipes(
    IdTorneioEquipe BIGSERIAL PRIMARY KEY,
    IdTorneio       BIGINT NOT NULL,
    IdEquipe        BIGINT NOT NULL,
    CONSTRAINT fk_torneiosequipes_idtorneio FOREIGN KEY (IdTorneio) REFERENCES torneios (IdTorneio) ON DELETE CASCADE,
    CONSTRAINT fk_torneiosequipes_idequipe  FOREIGN KEY (IdEquipe)  REFERENCES equipes (IdEquipe),
    CONSTRAINT ux_torneiosequipes UNIQUE (IdTorneio, IdEquipe)
);

CREATE TABLE lutas(
    IdLuta      BIGSERIAL PRIMARY KEY,
    IdTorneio   BIGINT NOT NULL,
    Fase        INTEGER NOT NULL,
    Ordem       INTEGER NOT NULL,
    IdEquipeA   BIGINT,
    IdEquipeB   BIGINT,
    IdVencedor  BIGINT,
    DtResultado TIMESTAMP,
    CONSTRAINT fk_lutas_idtorneio FOREIGN KEY (IdTorneio)   REFERENCES torneios (IdTorneio) ON DELETE CASCADE,
    CONSTRAINT fk_lutas_idequipea FOREIGN KEY (IdEquipeA)   REFERENCES equipes (IdEquipe),
    CONSTRAINT fk_lutas_idequipeb FOREIGN KEY (IdEquipeB)   REFERENCES equipes (IdEquipe),
    CONSTRAINT fk_lutas_idvencedor FOREIGN KEY (IdVencedor) REFERENCES equipes (IdEquipe),
    CONSTRAINT ux_lutas_posicao UNIQUE (IdTorneio, Fase, Ordem)
);