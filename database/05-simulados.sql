CREATE TABLE simulados(
    IdSimulado SERIAL PRIMARY KEY,
    IdAulaTema INTEGER NOT NULL,
    Titulo     VARCHAR(180) NOT NULL,
    CONSTRAINT fk_simulados_idaulatema FOREIGN KEY (IdAulaTema) REFERENCES aulastemas (IdAulaTema)
);

CREATE TABLE simuladosperguntas(
    IdSimuladoPergunta SERIAL PRIMARY KEY,
    IdSimulado         INTEGER NOT NULL,
    Enunciado          TEXT NOT NULL,
    CONSTRAINT fk_simuladosperguntas_idsimulado FOREIGN KEY (IdSimulado) REFERENCES simulados (IdSimulado) ON DELETE CASCADE
);

CREATE TABLE simuladosopcoes(
    IdSimuladoOpcao    SERIAL PRIMARY KEY,
    IdSimuladoPergunta INTEGER NOT NULL,
    Texto              VARCHAR(500) NOT NULL,
    Correta            BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_simuladosopcoes_idpergunta FOREIGN KEY (IdSimuladoPergunta) REFERENCES simuladosperguntas (IdSimuladoPergunta) ON DELETE CASCADE
);

CREATE TABLE simuladosrealizados(
    IdSimuladoRealizado SERIAL PRIMARY KEY,
    IdSimulado          INTEGER NOT NULL,
    IdSimuladoPergunta  INTEGER NOT NULL,  
    IdSimuladoOpcao     INTEGER NOT NULL,      
    RespostaCorreta     BOOLEAN NOT NULL DEFAULT FALSE,
    IdUsuario           BIGINT NOT NULL,
    DtTentativa         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Tentativa           INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_simuladosrealizados_idsimulado FOREIGN KEY (IdSimulado)         REFERENCES simulados (IdSimulado) ON DELETE CASCADE,
    CONSTRAINT fk_simuladosrealizados_idpergunta FOREIGN KEY (IdSimuladoPergunta) REFERENCES simuladosperguntas (IdSimuladoPergunta) ON DELETE CASCADE,
    CONSTRAINT fk_simuladosrealizados_idopcao    FOREIGN KEY (IdSimuladoOpcao)    REFERENCES simuladosopcoes (IdSimuladoOpcao) ON DELETE CASCADE,
    CONSTRAINT fk_simuladosrealizados_idusuario  FOREIGN KEY (IdUsuario)          REFERENCES usuarios (IdUsuario) ON DELETE CASCADE
);
CREATE INDEX ix_simuladosrealizados_usuario_simulado_tentativa ON simuladosrealizados (IdUsuario, IdSimulado, Tentativa);

CREATE TABLE simuladosrealizacoes (
    IdRealizacao BIGSERIAL PRIMARY KEY,
    IdUsuario BIGINT NOT NULL,
    IdSimulado INTEGER,
    DtRealizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_simuladosrealizacoes_usuario  FOREIGN KEY (IdUsuario)  REFERENCES usuarios (IdUsuario) ON DELETE CASCADE,
    CONSTRAINT fk_simuladosrealizacoes_simulado FOREIGN KEY (IdSimulado) REFERENCES simulados (IdSimulado) ON DELETE SET NULL
);
CREATE INDEX ix_simuladosrealizacoes_idusuario ON SimuladosRealizacoes (IdUsuario);
