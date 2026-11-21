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
ON usuarios
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
ON usuarios
FOR EACH ROW
EXECUTE FUNCTION usuariosupdatebefore();

CREATE OR REPLACE FUNCTION UsuariosInsertAfter()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.Status = 'Ativo' AND NEW.Perfil = 'aluno' THEN
        INSERT INTO aulasaluno (IdAula, IdAulaTema, IdUsuario, Concluida)
        SELECT a.IdAula, a.IdAulaTema, NEW.IdUsuario, 'N'
          FROM aulas a;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER UsuariosInsertAfter
AFTER INSERT
ON usuarios
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
ON aulas
FOR EACH ROW
EXECUTE FUNCTION AulasInsertBefore();

CREATE OR REPLACE FUNCTION AulasInsertAfter()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO aulasaluno (IdAula, IdAulaTema, IdUsuario, Concluida)
    SELECT NEW.IdAula, NEW.IdAulaTema, u.IdUsuario, 'N'
      FROM usuarios u
     WHERE u.Status = 'Ativo'
       AND u.Perfil = 'aluno';
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER AulasInsertAfter
AFTER INSERT
ON aulas
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
ON aulas
FOR EACH ROW
EXECUTE FUNCTION AulasUpdateBefore();

CREATE OR REPLACE FUNCTION AulasDeleteBefore()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM aulasaluno WHERE IdAula = OLD.IdAula;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER AulasDelete
BEFORE DELETE
ON aulas
FOR EACH ROW
EXECUTE FUNCTION AulasDeleteBefore();

CREATE OR REPLACE FUNCTION LogAlteracoesRegistrar()
RETURNS TRIGGER AS $$
DECLARE
    anterior JSONB := CASE WHEN TG_OP <> 'INSERT' THEN to_jsonb(OLD) ELSE '{}'::JSONB END;
    novo     JSONB := CASE WHEN TG_OP <> 'DELETE' THEN to_jsonb(NEW) ELSE '{}'::JSONB END;
    usuario  TEXT := COALESCE(NULLIF(CURRENT_SETTING('app.usuario', true), ''), CURRENT_USER);
    registro BIGINT := COALESCE(novo ->> TG_ARGV[0], anterior ->> TG_ARGV[0])::BIGINT;
    campo    TEXT;
BEGIN
    FOR campo IN SELECT jsonb_object_keys(anterior || novo) LOOP
        CONTINUE WHEN campo IN ('dtregistro', 'usrregistro', 'dtalteracao', 'usralteracao');
        CONTINUE WHEN (anterior -> campo) IS NOT DISTINCT FROM (novo -> campo);
        CONTINUE WHEN TG_OP <> 'UPDATE' AND jsonb_typeof(COALESCE(novo -> campo, anterior -> campo)) = 'null';
        IF campo IN ('senha', 'codigo') THEN
            INSERT INTO logalteracoes (Tabela, IdRegistro, Operacao, Campo, ValorAnterior, ValorNovo, Usuario)
            VALUES (LOWER(TG_TABLE_NAME), registro, TG_OP, campo, NULL, '[valor protegido alterado]', usuario);
        ELSE
            INSERT INTO logalteracoes (Tabela, IdRegistro, Operacao, Campo, ValorAnterior, ValorNovo, Usuario)
            VALUES (LOWER(TG_TABLE_NAME), registro, TG_OP, campo, anterior ->> campo, novo ->> campo, usuario);
        END IF;
    END LOOP;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS UsuariosLogAlteracoes ON Usuarios;
CREATE TRIGGER UsuariosLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON usuarios
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idusuario');

CREATE TRIGGER UsuariosSolicitacoesLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON usuariossolicitacoes
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idusuario');

CREATE TRIGGER SolicitacoesCadastroLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON solicitacoescadastro
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idsolicitacaocadastro');

CREATE TRIGGER SolicitacoesExclusaoLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON solicitacoesexclusao
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idsolicitacaoexclusao');

CREATE TRIGGER TurmasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON turmas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idturma');

CREATE TRIGGER UsuariosTurmasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON usuariosturmas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idusuarioturma');

CREATE TRIGGER EquipesLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON equipes
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idequipe');

CREATE TRIGGER EquipesMembrosLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON equipesmembros
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idequipemembro');

CREATE TRIGGER TorneiosLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON torneios
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idtorneio');

CREATE TRIGGER TorneiosEquipesLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON torneiosequipes
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idtorneioequipe');

CREATE TRIGGER LutasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON lutas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idluta');

CREATE TRIGGER AulasTemasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON aulastemas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idaulatema');

CREATE TRIGGER AulasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON aulas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idaula');

CREATE TRIGGER SimuladosLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON simulados
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idsimulado');

CREATE TRIGGER SimuladosPerguntasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON simuladoperguntas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idsimuladopergunta');

CREATE TRIGGER SimuladosOpcoesLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON simuladopcoes
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idsimuladoopcao');

CREATE TRIGGER TermosVersoesLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON termoversoes
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idtermoversao');