ALTER TABLE SolicitacoesExclusao ADD COLUMN IF NOT EXISTS UsrAnalise VARCHAR(255);

CREATE TABLE IF NOT EXISTS SolicitacoesCadastro(
    IdSolicitacaoCadastro BIGSERIAL PRIMARY KEY,
    Nome                  VARCHAR(50) NOT NULL,
    Email                 VARCHAR(255) NOT NULL,
    RA                    VARCHAR(11),
    Perfil                VARCHAR(20) NOT NULL,
    Senha                 VARCHAR(100),
    Status                VARCHAR(20) NOT NULL DEFAULT 'Pendente',
    Motivo                VARCHAR(2000),
    Ip                    VARCHAR(100),
    DtSolicitacao         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    DtAnalise             TIMESTAMP,
    UsrAnalise            VARCHAR(255)
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_solicitacoescadastro_pendente ON SolicitacoesCadastro (LOWER(Email)) WHERE Status = 'Pendente';

CREATE TABLE IF NOT EXISTS LogAlteracoes(
    IdLogAlteracao BIGSERIAL PRIMARY KEY,
    Tabela         VARCHAR(100) NOT NULL,
    IdRegistro     BIGINT,
    Operacao       VARCHAR(10) NOT NULL,
    Campo          VARCHAR(100) NOT NULL,
    ValorAnterior  TEXT,
    ValorNovo      TEXT,
    Usuario        VARCHAR(255),
    DtAlteracao    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_logalteracoes_registro ON LogAlteracoes (Tabela, IdRegistro);
CREATE INDEX IF NOT EXISTS ix_logalteracoes_dtalteracao ON LogAlteracoes (DtAlteracao DESC);

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
            INSERT INTO LogAlteracoes (Tabela, IdRegistro, Operacao, Campo, ValorAnterior, ValorNovo, Usuario)
            VALUES (LOWER(TG_TABLE_NAME), registro, TG_OP, campo, NULL, '[valor protegido alterado]', usuario);
        ELSE
            INSERT INTO LogAlteracoes (Tabela, IdRegistro, Operacao, Campo, ValorAnterior, ValorNovo, Usuario)
            VALUES (LOWER(TG_TABLE_NAME), registro, TG_OP, campo, anterior ->> campo, novo ->> campo, usuario);
        END IF;
    END LOOP;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS UsuariosLogAlteracoes ON Usuarios;
CREATE TRIGGER UsuariosLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON Usuarios
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idusuario');

DROP TRIGGER IF EXISTS UsuariosSolicitacoesLogAlteracoes ON UsuariosSolicitacoes;
CREATE TRIGGER UsuariosSolicitacoesLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON UsuariosSolicitacoes
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idusuario');

DROP TRIGGER IF EXISTS SolicitacoesCadastroLogAlteracoes ON SolicitacoesCadastro;
CREATE TRIGGER SolicitacoesCadastroLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON SolicitacoesCadastro
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idsolicitacaocadastro');

DROP TRIGGER IF EXISTS SolicitacoesExclusaoLogAlteracoes ON SolicitacoesExclusao;
CREATE TRIGGER SolicitacoesExclusaoLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON SolicitacoesExclusao
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idsolicitacaoexclusao');

DROP TRIGGER IF EXISTS TurmasLogAlteracoes ON Turmas;
CREATE TRIGGER TurmasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON Turmas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idturma');

DROP TRIGGER IF EXISTS UsuariosTurmasLogAlteracoes ON UsuariosTurmas;
CREATE TRIGGER UsuariosTurmasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON UsuariosTurmas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idusuarioturma');

DROP TRIGGER IF EXISTS EquipesLogAlteracoes ON Equipes;
CREATE TRIGGER EquipesLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON Equipes
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idequipe');

DROP TRIGGER IF EXISTS EquipesMembrosLogAlteracoes ON EquipesMembros;
CREATE TRIGGER EquipesMembrosLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON EquipesMembros
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idequipemembro');

DROP TRIGGER IF EXISTS TorneiosLogAlteracoes ON Torneios;
CREATE TRIGGER TorneiosLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON Torneios
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idtorneio');

DROP TRIGGER IF EXISTS TorneiosEquipesLogAlteracoes ON TorneiosEquipes;
CREATE TRIGGER TorneiosEquipesLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON TorneiosEquipes
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idtorneioequipe');

DROP TRIGGER IF EXISTS LutasLogAlteracoes ON Lutas;
CREATE TRIGGER LutasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON Lutas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idluta');

DROP TRIGGER IF EXISTS AulasTemasLogAlteracoes ON AulasTemas;
CREATE TRIGGER AulasTemasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON AulasTemas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idaulatema');

DROP TRIGGER IF EXISTS AulasLogAlteracoes ON Aulas;
CREATE TRIGGER AulasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON Aulas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idaula');

DROP TRIGGER IF EXISTS SimuladosLogAlteracoes ON Simulados;
CREATE TRIGGER SimuladosLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON Simulados
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idsimulado');

DROP TRIGGER IF EXISTS SimuladosPerguntasLogAlteracoes ON SimuladosPerguntas;
CREATE TRIGGER SimuladosPerguntasLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON SimuladosPerguntas
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idsimuladopergunta');

DROP TRIGGER IF EXISTS SimuladosOpcoesLogAlteracoes ON SimuladosOpcoes;
CREATE TRIGGER SimuladosOpcoesLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON SimuladosOpcoes
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idsimuladoopcao');

DROP TRIGGER IF EXISTS TermosVersoesLogAlteracoes ON TermosVersoes;
CREATE TRIGGER TermosVersoesLogAlteracoes
AFTER INSERT OR UPDATE OR DELETE
ON TermosVersoes
FOR EACH ROW
EXECUTE FUNCTION LogAlteracoesRegistrar('idtermoversao');

INSERT INTO TermosVersoes (Versao, DtVersao, Conteudo)
SELECT '0.4', '2026-11-19', $termo$# Informações gerais

## 1 APRESENTAÇÃO E RESPONSÁVEIS

O Robonet é uma plataforma web de apoio à aprendizagem de robótica educacional e ao gerenciamento de equipes e torneios. O sistema oferece aulas, simulados, turmas, equipes, torneios no formato mata-mata com ranking e a área Meus dados. Novos recursos serão disponibilizados gradualmente, conforme o desenvolvimento do sistema.

Nesta fase, Paola dos Santos Ramos e Bruno Bernardelli Fraudio são os responsáveis pelo Robonet e pelas decisões sobre o tratamento dos dados pessoais, na condição de controladores. A faculdade não participa dessa gestão nesta etapa. Contato para suporte e privacidade: robonet.umc@gmail.com.

A disponibilização inicial ocorre em fase de testes com alunos e professores reais, em ambiente de nuvem. Durante a fase de testes do PFC, o uso do Robonet será gratuito para alunos e professores participantes. Recursos futuros somente integrarão o serviço após sua disponibilização e as informações pertinentes aos usuários.

## 2 ACESSO EXCLUSIVO PARA ADULTOS

O uso é permitido exclusivamente a pessoas com 18 anos completos ou mais. O usuário deverá confirmar que atende a esse requisito antes de acessar os recursos da conta. A autorização de um responsável não substitui essa condição de acesso.

Se houver indicação de uso por pessoa menor de 18 anos, a equipe poderá restringir o acesso para apuração e regularização. A verificação deverá utilizar apenas os dados necessários. Os dados eventualmente recebidos terão tratamento adequado à legislação de proteção de crianças e adolescentes, sem autorização para continuidade de uso.

## 3 CADASTRO E AUTENTICAÇÃO

A criação e a manutenção das contas são realizadas pela administração autorizada. O usuário deverá conferir seus dados, solicitar correções e proteger sua senha e seus códigos de acesso. O login utiliza e-mail e senha e, quando a verificação no login estiver ativada, código enviado ao endereço cadastrado. Alunos e professores podem ativar ou desativar essa verificação em Meus dados, e a administração também pode configurá-la. O Robonet não solicita a senha da conta Google do usuário.

Quem ainda não possui conta pode usar a opção Não sou cadastrado, na tela de login, para enviar uma solicitação de cadastro com nome, e-mail, perfil, RA (para aluno) e senha, que é armazenada apenas em formato de hash. A conta somente é criada depois da aprovação de um administrador. Em caso de recusa, o motivo é registrado e a senha é descartada.

Na área Meus dados, alunos e professores podem consultar o próprio cadastro, solicitar alteração de nome, RA, perfil ou turma, ativar ou desativar a verificação por código no login e alterar a senha, que deve ter de 8 a 50 caracteres, com letra maiúscula, letra minúscula, número e caractere especial.

# Regras de uso e aceite

## 4 CONDUTAS E CONTEÚDO

É proibido acessar contas ou informações sem autorização, compartilhar credenciais, explorar falhas, inserir código malicioso, praticar discriminação ou utilizar a plataforma para atividades ilícitas. O usuário deve comunicar suspeitas de acesso indevido ao canal de contato.

Quem cadastrar aulas ou outros conteúdos deverá ter autorização para utilizá-los e evitar dados pessoais de terceiros sem necessidade e fundamento legítimo. Não devem ser inseridos dados de saúde, vida sexual ou outros dados sensíveis nos campos livres destinados a conteúdo didático.

Os direitos sobre materiais inseridos permanecem com seus titulares. O Robonet recebe apenas a autorização necessária para armazenar, organizar e exibir esses materiais às pessoas autorizadas, na medida necessária à operação. Nas apresentações e demonstrações do PFC, serão utilizados dados fictícios, sem exposição dos dados pessoais ou resultados reais dos participantes.

## 5 DISPONIBILIDADE E RESPONSABILIDADES

Podem ocorrer interrupções por manutenção, falhas técnicas ou indisponibilidade de fornecedores. Alterações relevantes e interrupções planejadas serão informadas sempre que possível. A plataforma não promete funcionamento ininterrupto nem substitui comunicações oficiais da instituição de ensino.

As responsabilidades de cada parte serão apuradas conforme a legislação. Nenhuma cláusula exclui direitos do usuário ou afasta a responsabilidade dos responsáveis por falhas que legalmente lhes sejam atribuíveis. O caráter acadêmico do projeto não elimina obrigações aplicáveis.

## 6 SUSPENSÃO E ENCERRAMENTO

O acesso poderá ser restringido por violação deste termo, perda da autorização de uso ou necessidade de segurança. A medida deverá ser proporcional, com informação do motivo quando possível e oportunidade de solicitar revisão pelo canal de contato. Medidas urgentes poderão anteceder a comunicação.

Alunos e professores podem solicitar a exclusão da própria conta em Meus dados, conforme a seção 17; os demais usuários devem utilizar o canal de contato. A desativação do acesso por um administrador não equivale à eliminação dos dados; o tratamento após o encerramento segue o Aviso de Privacidade e as hipóteses legais de conservação.

## 7 ALTERAÇÕES E LEGISLAÇÃO

Mudanças relevantes serão comunicadas antes de produzir efeitos, ressalvadas medidas urgentes ou exigências legais. Novas finalidades não serão autorizadas por silêncio. Aplicam-se as leis brasileiras, preservados os direitos de proteção de dados e, quando cabíveis, de consumidor, inclusive o acesso ao foro legalmente competente.

## 8 DECLARAÇÃO PARA ACEITE

“Declaro ter 18 anos completos ou mais, li e aceito o Termo de Uso do Robonet e tive acesso ao Aviso de Privacidade.” O aceite não representa consentimento genérico para qualquer tratamento. Autorizações opcionais e consentimentos específicos, quando necessários, serão apresentados separadamente.

# Aviso de Privacidade

## 9 QUEM RESPONDE PELOS DADOS

Os controladores nesta fase são Paola dos Santos Ramos e Bruno Bernardelli Fraudio. O canal para assuntos de dados pessoais é robonet.umc@gmail.com. Caso uma instituição passe a participar das decisões sobre os dados, os usuários serão informados sobre sua identidade e as responsabilidades correspondentes antes da mudança.

## 10 DADOS E FINALIDADES

Identificação e vínculo: nome, e-mail, RA (Registro do Aluno) para o perfil de aluno, identificador da conta, perfil de acesso, situação ativa ou inativa, turmas e equipe. O RA será usado para identificar cada aluno e vincular seu progresso à conta correta. Os demais dados permitem organizar o cadastro e disponibilizar os recursos apropriados.

Autenticação: senha armazenada em formato de hash, códigos de verificação também armazenados em formato de hash, com finalidade, momento da geração e número de tentativas, limitado a 5 (cinco) por código, e credenciais de sessão. São utilizados para verificar o acesso e recuperar a conta. Os códigos não se destinam a publicidade ou análise de comportamento.

Operação e auditoria: registros de acesso e de auditoria com e-mail, endereço IP da conexão e data/hora, incluindo logins realizados, tentativas de login com senha ou código inválido, saídas da plataforma, tentativas de acesso negadas por falta de autenticação ou de permissão, criação e alteração de senhas (sem guardar a senha, apenas o fato ocorrido), alterações de e-mail, com os endereços anterior e novo, cópias de dados geradas, pedidos de exclusão de conta e operações realizadas em usuários, aulas, simulados, turmas, equipes e torneios, com seus responsáveis, além de títulos, temas e conteúdo de aulas. Servem à segurança, à rastreabilidade das ações, à operação, ao suporte e à apuração de incidentes, e são consultados apenas por administradores autorizados.

Histórico de alterações: sempre que um registro do sistema é incluído, alterado ou excluído, o banco de dados grava automaticamente a tabela, o identificador do registro, o campo alterado, o valor anterior, o novo valor, a operação, quem a realizou e a data/hora. Senhas e códigos de verificação não são gravados nesse histórico. Ele serve à rastreabilidade e à correção de erros e é consultado apenas por administradores autorizados.

Na área de administração, o e-mail dos usuários e os dados das solicitações de cadastro e de alteração aparecem mascarados. Eles só são exibidos quando o administrador pede a visualização ou abre a análise, e cada visualização é registrada na auditoria como acesso a dado sensível.

O registro de aceite identifica o usuário, a versão aceita, a data e o endereço IP do ato. A recusa do termo também é registrada, com e-mail, data e endereço IP, e impede o acesso aos recursos da conta até que o termo seja aceito.

Progresso de aprendizagem: o Robonet registra as aulas concluídas e os simulados realizados pelo aluno, com a respectiva data. Em cada tentativa de simulado são armazenadas as respostas escolhidas, a indicação de acerto de cada resposta, o número da tentativa e a data, que permitem calcular o resultado. O aluno consulta o próprio progresso. Na tela de aulas, o professor visualiza o nome, o e-mail e a conclusão de cada aula somente dos alunos das turmas a que está vinculado, e os administradores visualizam essas informações de todos os alunos. No painel, professores e administradores visualizam apenas totais gerais, sem identificação dos alunos.

Solicitações de alteração de dados: nome, e-mail, RA, perfil e turma informados pelo usuário, situação, motivo do indeferimento e datas do pedido e da análise. São utilizados para que a administração analise e aplique a correção solicitada.

Solicitações de cadastro: nome, e-mail, perfil, RA para aluno, senha em formato de hash, endereço IP, situação, motivo da recusa, datas do pedido e da análise e o administrador responsável. São utilizados para que a administração decida sobre a criação da conta.

Turmas e equipes: professores e administradores acessam o nome e o RA dos alunos para organizar turmas e equipes, sendo o acesso do professor limitado às turmas a que está vinculado. O aluno visualiza apenas a própria equipe, com o nome da equipe, a turma, o nome dos integrantes e os torneios de que a equipe participou ou participará.

Torneios: os nomes dos torneios, seus locais, datas e regras, o nome do professor responsável, os nomes e as turmas das equipes, o chaveamento, os vencedores das lutas, as pontuações e as classificações podem ser consultados pelos usuários autenticados. Cada luta vencida vale 1 ponto. Essa visualização não inclui e-mails, RA, integrantes das equipes nem o progresso individual dos alunos.

Os dados são fornecidos pelo usuário, por administrador autorizado ou gerados pelo uso. Quando um administrador cadastrar alguém, as informações deste aviso deverão ser disponibilizadas antes do primeiro uso. O usuário poderá solicitar informação sobre a origem dos seus dados.

## 11 BASES LEGAIS

O tratamento necessário para prestar as funcionalidades solicitadas será fundamentado na execução da relação de uso, quando aplicável, nos termos do art. 7º, V, da LGPD. Conservação por obrigação legal ou exercício de direitos depende de hipótese efetivamente existente, identificada e documentada.

Atividades de segurança baseadas em legítimo interesse exigem avaliação de necessidade, proporcionalidade e impacto sobre o titular. Tratamentos opcionais baseados em consentimento terão finalidade específica, escolha livre e possibilidade de revogação. A condição de PFC não autoriza automaticamente o uso de qualquer dado para pesquisa.

## 12 DADOS FORA DO CADASTRO

O campo sexo não será coletado nesta fase. Não se solicita autorização genérica para coletar novos dados. Eventual ampliação do cadastro deverá ser necessária à finalidade, ter base legal adequada e ser informada ao usuário antes da coleta.

# Google e compartilhamento de dados

## 13 ENVIO DE E-MAILS PELO GMAIL

O Robonet utiliza a API do Gmail, com a conta remetente da plataforma, para enviar códigos de acesso e de recuperação de senha ao e-mail cadastrado e códigos de confirmação ao novo endereço informado na troca de e-mail. Para isso, o endereço do destinatário e o conteúdo da mensagem passam pela infraestrutura do Google e do provedor de e-mail do destinatário.

Essa funcionalidade não conecta a caixa postal do usuário ao Robonet e não concede acesso às mensagens recebidas, aos contatos ou à senha Google do destinatário. A permissão de envio pertence à conta remetente autorizada. As mensagens de segurança são necessárias ao fluxo atual de autenticação.

## 14 GOOGLE AGENDA

Para os torneios da sua equipe que ainda não aconteceram, a tela Minha equipe oferece o botão Adicionar ao Google Agenda. Ao clicar, o navegador abre a página do Google Agenda já preenchida com o nome, as datas e o local do torneio, e o próprio usuário decide se salva o evento.

O Robonet não se conecta à conta Google do usuário, não pede permissões de agenda e não lê, cria, altera nem exclui eventos. Os dados do torneio só são enviados ao Google pelo navegador quando o usuário usa o botão, e o evento salvo passa a seguir as regras de privacidade do Google.

## 15 DESTINATÁRIOS E ACESSO INTERNO

Administradores autorizados poderão acessar os dados necessários às suas atribuições. Fornecedores somente deverão receber os dados necessários ao serviço contratado, com responsabilidades e medidas de proteção definidas. Compartilhamento com autoridades dependerá de fundamento legal e das formalidades aplicáveis.

Não se autoriza, por este termo, venda de dados, publicidade comportamental ou utilização de dados pessoais identificáveis em apresentações acadêmicas. O uso de dados obtidos das APIs Google deve respeitar as políticas dessas APIs, inclusive as restrições de uso limitado quando aplicáveis.

## 16 HOSPEDAGEM E TRANSFERÊNCIAS INTERNACIONAIS

O Robonet utiliza os fornecedores abaixo, que tratam dados pessoais apenas para prestar o serviço contratado e não podem usá-los para finalidades próprias.

Vercel: distribui a interface do sistema, ou seja, as páginas e os arquivos do site. Recebe o endereço IP e os dados técnicos do navegador de quem acessa, sem receber o conteúdo do cadastro.

Render: executa o servidor da aplicação, em servidores localizados nos Estados Unidos (região de Ohio). Processa os dados enviados e consultados durante o uso da plataforma.

Supabase: armazena o banco de dados e suas cópias de segurança, em servidores localizados nos Estados Unidos (região de Ohio). Guarda os dados descritos na seção 10.

Google: envia os e-mails com códigos de acesso, de recuperação de senha e de confirmação de e-mail pela API do Gmail, conforme a seção 13, e recebe os dados do torneio quando o usuário usa o botão Adicionar ao Google Agenda, conforme a seção 14.

Por isso, os dados pessoais tratados pelo Robonet são armazenados e processados fora do Brasil. Essa transferência internacional é necessária para executar as funcionalidades solicitadas pelo usuário e se fundamenta no art. 33, IX, combinado com o art. 7º, V, da LGPD, observada a regulamentação da ANPD sobre transferência internacional de dados (Resolução CD/ANPD nº 19/2024).

A comunicação entre o navegador, o servidor e o banco de dados é criptografada (HTTPS e SSL), e o acesso aos servidores é restrito aos controladores. Os controladores mantêm registro dos fornecedores, dos dados transferidos e das garantias contratuais de proteção de dados oferecidas por cada um, e essas informações podem ser solicitadas pelo canal robonet.umc@gmail.com. Se um fornecedor ou o país de hospedagem mudar, este aviso será atualizado antes da mudança.

# Conservação e direitos do usuário

## 17 PRAZO E ENCERRAMENTO DO TRATAMENTO

O Robonet conserva os dados pelo tempo necessário à finalidade informada, observados os seguintes prazos: dados cadastrais de conta desativada sem pedido de exclusão, por 6 (seis) meses contados do encerramento da conta; registros de acesso e de auditoria, por 6 (seis) meses contados do seu registro; registros de aceite e pedidos de exclusão de conta encerrada, por 6 (seis) meses contados do encerramento; histórico de alterações de dados, por 6 (seis) meses contados do registro; códigos de verificação, por 24 (vinte e quatro) horas contadas da sua geração; cópias de segurança, em ciclo de descarte de 30 (trinta) dias.

Prazo maior somente se aplica quando houver obrigação legal ou necessidade de exercício de direitos em processo, hipótese que será identificada e documentada. Nesta fase de testes, a eliminação ao término desses prazos é executada por procedimento manual conduzido pelos controladores; a automatização está prevista para as versões seguintes.

Exclusão a pedido: em Meus dados, alunos e professores podem solicitar a exclusão da conta informando a senha atual. O pedido recebe uma data limite de análise de 14 (quatorze) dias, exibida em Meus dados. Um administrador pode aprová-lo antes dessa data e, se não houver análise até a data limite, a exclusão é aprovada automaticamente pelo sistema, em atenção à LGPD. Até a conclusão, o acesso é mantido e o pedido pode ser cancelado. Concluída a exclusão, o acesso é encerrado de forma definitiva: nome, RA, e-mail e senha são removidos do cadastro, que passa a constar como “Usuário removido”, e são eliminados os vínculos com turmas e equipes, as aulas concluídas, os simulados realizados com as respostas e os resultados, as solicitações de alteração de dados e de cadastro, o histórico de alterações do cadastro e dessas solicitações e os códigos de verificação.

Para fins de auditoria, segurança e comprovação do atendimento, são mantidos, pelos prazos acima, os registros de acesso e de auditoria, os registros de aceite e o pedido de exclusão, que podem conter e-mail, endereço IP e data/hora. As cópias de segurança são descartadas conforme seu ciclo. Aulas, simulados, equipes e torneios criados pelo usuário permanecem, por serem necessários aos demais usuários, e os resultados de torneios são mantidos sem exibir o nome do usuário excluído. Os campos internos de autoria desses registros, acessíveis apenas à administração, podem conservar sua identificação para fins de auditoria, pelo tempo em que os registros existirem.

A validade de um código de acesso não é o mesmo que seu prazo de armazenamento. A desativação da conta também não elimina automaticamente o cadastro. Não haverá conservação indefinida por mera conveniência.

## 18 ARMAZENAMENTO NO NAVEGADOR

O Robonet utiliza armazenamento local do navegador para manter dados de sessão, incluindo token e resumo da conta. A ação de sair remove esse registro local. Apagar os dados do site no navegador não elimina o cadastro mantido no servidor. Novas ferramentas de rastreamento exigirão informação prévia e base legal adequada.

## 19 SEUS DIREITOS E ATENDIMENTO

Você pode solicitar confirmação e acesso aos dados, correção, informação sobre compartilhamentos, anonimização, bloqueio ou eliminação de dados desnecessários ou tratados irregularmente, além de portabilidade conforme a regulamentação. Quando houver consentimento, pode revogá-lo e pedir a eliminação, ressalvadas as hipóteses legais de conservação.

Também pode opor-se a tratamento sem consentimento que descumpra a LGPD e solicitar revisão de decisões unicamente automatizadas que afetem seus interesses, quando existirem. É possível apresentar reclamação à ANPD e aos órgãos competentes.

Alunos e professores podem exercer diretamente em Meus dados os seguintes direitos: consultar o cadastro; solicitar alteração de nome, RA, perfil ou turma; alterar a senha; ativar ou desativar a verificação por código no login; baixar uma cópia dos dados em arquivo JSON, com cadastro, situação da conta, data de cadastro, uso de código no login, turmas, equipe, aulas concluídas, simulados realizados com respostas e resultados, solicitações de alteração de dados, pedidos de exclusão, aceites do termo, acessos e registros de auditoria das ações realizadas pelo próprio usuário, sem a senha e os códigos de verificação, que são guardados apenas em formato de hash, que pode ser guardada ou compartilhada como o usuário preferir; e solicitar a exclusão da conta, conforme a seção 17.

Para os demais pedidos, envie sua solicitação a robonet.umc@gmail.com. Nesse canal, a identidade será verificada de forma proporcional, sem solicitar senha ou código de acesso; em Meus dados, a senha atual é exigida apenas nas ações que a solicitam na própria tela. O atendimento é gratuito. Confirmação e acesso seguem o art. 19 da LGPD: resposta simplificada imediata ou declaração completa em até 15 dias, ressalvadas regras específicas aplicáveis. Os demais pedidos seguem os prazos legais pertinentes.

## 20 SEGURANÇA E INCIDENTES

Os responsáveis deverão adotar medidas técnicas e administrativas adequadas, limitar acessos e proteger credenciais. Incidentes serão avaliados e, quando houver risco ou dano relevante, comunicados aos titulares e à ANPD nos prazos aplicáveis. O canal de contato também recebe relatos de segurança. Este documento não substitui a implementação dessas medidas.$termo$
WHERE NOT EXISTS (SELECT 1 FROM TermosVersoes WHERE Versao = '0.4');
