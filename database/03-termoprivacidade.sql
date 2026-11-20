DROP TABLE IF EXISTS TermosAceites;
DROP TABLE IF EXISTS TermosVersoes;

CREATE TABLE TermosVersoes(
    IdTermoVersao SERIAL PRIMARY KEY,
    Versao        VARCHAR(20) NOT NULL UNIQUE,
    DtVersao      DATE NOT NULL,
    Conteudo      TEXT NOT NULL,
    DtRegistro    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TermosAceites(
    IdTermoAceite BIGSERIAL PRIMARY KEY,
    IdUsuario     BIGINT NOT NULL,
    Versao        VARCHAR(20) NOT NULL,
    Ip            VARCHAR(100),
    DtAceite      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_termo_idusuario FOREIGN KEY (IdUsuario) REFERENCES Usuarios (IdUsuario),
    CONSTRAINT fk_termo_versao FOREIGN KEY (Versao) REFERENCES TermosVersoes (Versao)
);
