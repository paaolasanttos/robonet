CREATE TABLE equipes(
    IdEquipe   BIGSERIAL PRIMARY KEY,
    Nome       VARCHAR(120) NOT NULL,
    IdTurma    BIGINT NOT NULL,
    DtRegistro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipes_idturma FOREIGN KEY (IdTurma) REFERENCES turmas (IdTurma)
);
CREATE UNIQUE INDEX ux_equipes_nome ON equipes (IdTurma, LOWER(TRIM(Nome)));

CREATE TABLE equipesmembros(
    IdEquipeMembro BIGSERIAL PRIMARY KEY,
    IdEquipe       BIGINT NOT NULL,
    IdUsuario      BIGINT NOT NULL,
    CONSTRAINT fk_membros_idequipe  FOREIGN KEY (IdEquipe)  REFERENCES equipes (IdEquipe) ON DELETE CASCADE,
    CONSTRAINT fk_membros_idusuario FOREIGN KEY (IdUsuario) REFERENCES usuarios (IdUsuario),
    CONSTRAINT ux_membros_idusuario UNIQUE (IdUsuario)
);