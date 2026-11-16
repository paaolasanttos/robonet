DROP TABLE IF EXISTS SimuladosRealizados;
DROP TABLE IF EXISTS SimuladosOpcoes;
DROP TABLE IF EXISTS SimuladosPerguntas;
DROP TABLE IF EXISTS Simulados;

CREATE TABLE Simulados(
    IdSimulado SERIAL PRIMARY KEY,
    IdAulaTema INTEGER NOT NULL,
    Titulo     VARCHAR(180) NOT NULL,
    CONSTRAINT fk_simulados_idaulatema FOREIGN KEY (IdAulaTema) REFERENCES AulasTemas (IdAulaTema)
);

CREATE TABLE SimuladosPerguntas(
    IdSimuladoPergunta SERIAL PRIMARY KEY,
    IdSimulado         INTEGER NOT NULL,
    Enunciado          TEXT NOT NULL,
    CONSTRAINT fk_simuladosperguntas_idsimulado FOREIGN KEY (IdSimulado) REFERENCES Simulados (IdSimulado) ON DELETE CASCADE
);

CREATE TABLE SimuladosOpcoes(
    IdSimuladoOpcao    SERIAL PRIMARY KEY,
    IdSimuladoPergunta INTEGER NOT NULL,
    Texto              VARCHAR(500) NOT NULL,
    Correta            BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_simuladosopcoes_idpergunta FOREIGN KEY (IdSimuladoPergunta) REFERENCES SimuladosPerguntas (IdSimuladoPergunta) ON DELETE CASCADE
);

CREATE TABLE SimuladosRealizados(
    IdSimuladoRealizado SERIAL PRIMARY KEY,
    IdSimulado          INTEGER NOT NULL,
    IdSimuladoPergunta  INTEGER NOT NULL,  
    IdSimuladoOpcao     INTEGER NOT NULL,      
    RespostaCorreta     BOOLEAN NOT NULL DEFAULT FALSE,
    IdUsuario           BIGSERIAL NOT NULL,
    DtTentativa         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Tentativa           INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_simuladosrealizados_idsimulado FOREIGN KEY (IdSimulado) REFERENCES Simulados (IdSimulado) ON DELETE CASCADE,
    CONSTRAINT fk_simuladosrealizados_idpergunta FOREIGN KEY (IdSimuladoPergunta) REFERENCES SimuladosPerguntas (IdSimuladoPergunta) ON DELETE CASCADE,
    CONSTRAINT fk_simuladosrealizados_idopcao FOREIGN KEY (IdSimuladoOpcao) REFERENCES SimuladosOpcoes (IdSimuladoOpcao) ON DELETE CASCADE,
    CONSTRAINT fk_simuladosrealizados_idusuario FOREIGN KEY (IdUsuario) REFERENCES Usuarios (IdUsuario) ON DELETE CASCADE
);

CREATE INDEX ix_simuladosrealizados_usuario_simulado_tentativa
    ON SimuladosRealizados (IdUsuario, IdSimulado, Tentativa);