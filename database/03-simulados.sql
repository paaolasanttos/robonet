CREATE TABLE IF NOT EXISTS Simulados(
    IdSimulado SERIAL PRIMARY KEY,
    IdAulaTema INTEGER NOT NULL,
    Titulo     VARCHAR(180) NOT NULL,
    CONSTRAINT fk_simulados_idaulatema FOREIGN KEY (IdAulaTema) REFERENCES AulasTemas (IdAulaTema)
);

CREATE TABLE IF NOT EXISTS SimuladosPerguntas(
    IdSimuladoPergunta SERIAL PRIMARY KEY,
    IdSimulado         INTEGER NOT NULL,
    Enunciado          TEXT NOT NULL,
    CONSTRAINT fk_simuladosperguntas_idsimulado FOREIGN KEY (IdSimulado) REFERENCES Simulados (IdSimulado) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS SimuladosOpcoes(
    IdSimuladoOpcao    SERIAL PRIMARY KEY,
    IdSimuladoPergunta INTEGER NOT NULL,
    Texto              VARCHAR(500) NOT NULL,
    Correta            BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_simuladosopcoes_idpergunta FOREIGN KEY (IdSimuladoPergunta) REFERENCES SimuladosPerguntas (IdSimuladoPergunta) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION UsuariosInsertAfter()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.Status = 'Ativo' AND NEW.Perfil = 'aluno' THEN
        INSERT INTO AulasAluno (IdAula, IdAulaTema, IdUsuario, Concluida)
        SELECT a.IdAula, a.IdAulaTema, NEW.IdUsuario, 'N'
          FROM Aulas a;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS UsuariosInsertAfter ON Usuarios;
CREATE TRIGGER UsuariosInsertAfter
AFTER INSERT
ON Usuarios
FOR EACH ROW
EXECUTE FUNCTION UsuariosInsertAfter();

INSERT INTO AulasAluno (IdAula, IdAulaTema, IdUsuario, Concluida)
SELECT a.IdAula, a.IdAulaTema, u.IdUsuario, 'N'
  FROM Aulas a
 CROSS JOIN Usuarios u
 WHERE u.Status = 'Ativo'
   AND u.Perfil = 'aluno'
   AND NOT EXISTS (
       SELECT 1
         FROM AulasAluno aa
        WHERE aa.IdAula = a.IdAula
          AND aa.IdUsuario = u.IdUsuario
   );
