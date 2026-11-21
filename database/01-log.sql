CREATE TABLE logacessos(
    IdLogAcesso BIGSERIAL PRIMARY KEY,
    Email       VARCHAR(2000) NOT NULL,
    DtAcesso    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE logcodigoverificacao(
    IdLogCodigoVerificacao BIGSERIAL PRIMARY KEY,
    Email                  VARCHAR(2000) NOT NULL,
    Codigo                 VARCHAR(100) NOT NULL,
    Tipo                   VARCHAR(50) NOT NULL,
    Tentativas             INTEGER NOT NULL DEFAULT 0,
    DtGeracao              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE logauditoria(
    IdLogAuditoria BIGSERIAL PRIMARY KEY,
    Email          VARCHAR(2000),
    Categoria      VARCHAR(50) NOT NULL,
    Acao           VARCHAR(100) NOT NULL,
    Descricao      TEXT,
    Ip             VARCHAR(100),
    Sucesso        BOOLEAN NOT NULL DEFAULT TRUE,
    DtRegistro     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_logauditoria_dtregistro ON logauditoria (DtRegistro DESC);

CREATE TABLE logalteracoes(
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
CREATE INDEX IF NOT EXISTS ix_logalteracoes_registro ON logalteracoes (Tabela, IdRegistro);
CREATE INDEX IF NOT EXISTS ix_logalteracoes_dtalteracao ON logalteracoes (DtAlteracao DESC);
