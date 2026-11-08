DROP TABLE IF EXISTS TermosAceites;

CREATE TABLE TermosAceites(
    IdTermoAceite BIGSERIAL PRIMARY KEY,
    IdUsuario     BIGINT NOT NULL,
    Versao        VARCHAR(20) NOT NULL,
    Ip            VARCHAR(100),
    DtAceite      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_termo_idusuario FOREIGN KEY (IdUsuario) REFERENCES Usuarios (IdUsuario)
);