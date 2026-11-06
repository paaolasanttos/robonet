CREATE TABLE IF NOT EXISTS Turmas(
    IdTurma    BIGSERIAL PRIMARY KEY,
    Nome       VARCHAR(120) NOT NULL,
    DtRegistro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_turmas_nome
    ON Turmas (LOWER(TRIM(Nome)));

CREATE TABLE IF NOT EXISTS UsuariosTurmas(
    IdUsuarioTurma BIGSERIAL PRIMARY KEY,
    IdUsuario      BIGINT NOT NULL,
    IdTurma        BIGINT NOT NULL,
    CONSTRAINT fk_usuariosturmas_idusuario FOREIGN KEY (IdUsuario) REFERENCES Usuarios (IdUsuario),
    CONSTRAINT fk_usuariosturmas_idturma FOREIGN KEY (IdTurma) REFERENCES Turmas (IdTurma),
    CONSTRAINT ux_usuariosturmas UNIQUE (IdUsuario, IdTurma)
);
