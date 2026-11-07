CREATE TABLE IF NOT EXISTS SolicitacoesExclusao(
    IdSolicitacaoExclusao BIGSERIAL PRIMARY KEY,
    IdUsuario             BIGINT NOT NULL,
    Email                 VARCHAR(2000) NOT NULL,
    Status                VARCHAR(20) NOT NULL DEFAULT 'Pendente',
    Ip                    VARCHAR(100),
    DtSolicitacao         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    DtLimite              TIMESTAMP NOT NULL,
    DtConclusao           TIMESTAMP,
    CONSTRAINT fk_solicitacoesexclusao_idusuario FOREIGN KEY (IdUsuario) REFERENCES Usuarios (IdUsuario)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_solicitacoesexclusao_pendente
    ON SolicitacoesExclusao (IdUsuario) WHERE Status = 'Pendente';
