CREATE TABLE turmas(
    IdTurma    BIGSERIAL PRIMARY KEY,
    Nome       VARCHAR(120) NOT NULL,
    DtRegistro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX ux_turmas_nome ON turmas (LOWER(TRIM(Nome)));

CREATE TABLE usuariosturmas(
    IdUsuarioTurma BIGSERIAL PRIMARY KEY,
    IdUsuario      BIGINT NOT NULL,
    IdTurma        BIGINT NOT NULL,
    CONSTRAINT fk_usuariosturmas_idusuario FOREIGN KEY (IdUsuario) REFERENCES usuarios (IdUsuario),
    CONSTRAINT fk_usuariosturmas_idturma   FOREIGN KEY (IdTurma)   REFERENCES turmas (IdTurma),
    CONSTRAINT ux_usuariosturmas UNIQUE (IdUsuario, IdTurma)
);