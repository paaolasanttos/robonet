CREATE OR REPLACE FUNCTION usuariosinsertbefore()
RETURNS TRIGGER AS $$
BEGIN
    NEW.DtRegistro := CURRENT_TIMESTAMP;
    NEW.UsrRegistro := COALESCE(NULLIF(CURRENT_SETTING('app.usuario', true), ''),CURRENT_USER);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER UsuariosInsert
BEFORE INSERT
ON Usuarios
FOR EACH ROW
EXECUTE FUNCTION usuariosinsertbefore();

CREATE OR REPLACE FUNCTION usuariosupdatebefore()
RETURNS TRIGGER AS $$
BEGIN
    NEW.DtAlteracao := CURRENT_TIMESTAMP;
    NEW.UsrAlteracao := COALESCE(NULLIF(CURRENT_SETTING('app.usuario', true), ''),CURRENT_USER);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER UsuariosUpdate
BEFORE UPDATE
ON Usuarios
FOR EACH ROW
EXECUTE FUNCTION usuariosupdatebefore();

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

CREATE TRIGGER UsuariosInsertAfter
AFTER INSERT
ON Usuarios
FOR EACH ROW
EXECUTE FUNCTION UsuariosInsertAfter();

CREATE OR REPLACE FUNCTION AulasInsertBefore()
RETURNS TRIGGER AS $$
BEGIN
    NEW.DtRegistro := CURRENT_TIMESTAMP;
    NEW.UsrRegistro := COALESCE(NULLIF(CURRENT_SETTING('app.usuario', true), ''),CURRENT_USER);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER AulasInsert
BEFORE INSERT
ON Aulas
FOR EACH ROW
EXECUTE FUNCTION AulasInsertBefore();

CREATE OR REPLACE FUNCTION AulasInsertAfter()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO AulasAluno (IdAula, IdAulaTema, IdUsuario, Concluida)
    SELECT NEW.IdAula, NEW.IdAulaTema, u.IdUsuario, 'N'
      FROM Usuarios u
     WHERE u.Status = 'Ativo'
       AND u.Perfil = 'aluno';
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER AulasInsertAfter
AFTER INSERT
ON Aulas
FOR EACH ROW
EXECUTE FUNCTION AulasInsertAfter();

CREATE OR REPLACE FUNCTION AulasUpdateBefore()
RETURNS TRIGGER AS $$
BEGIN
    NEW.DtAlteracao := CURRENT_TIMESTAMP;
    NEW.UsrAlteracao := COALESCE(NULLIF(CURRENT_SETTING('app.usuario', true), ''),CURRENT_USER);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER AulasUpdate
BEFORE UPDATE
ON Aulas
FOR EACH ROW
EXECUTE FUNCTION AulasUpdateBefore();

CREATE OR REPLACE FUNCTION AulasDeleteBefore()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM AulasAluno WHERE IdAula = OLD.IdAula;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER AulasDelete
BEFORE DELETE
ON Aulas
FOR EACH ROW
EXECUTE FUNCTION AulasDeleteBefore();