INSERT INTO Usuarios(Nome,email,Perfil,Status,AcessoMFV) 
VALUES('Administrador','robonet.umc@gmail.com','admin','Ativo','N');

INSERT INTO AulasTemas(Descricao)
VALUES ('Introdução à Robótica'), ('Programação'),('Projeto final');

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