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