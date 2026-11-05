CREATE TABLE IF NOT EXISTS SimuladosRealizacoes (
    IdRealizacao BIGSERIAL PRIMARY KEY,
    IdUsuario BIGINT NOT NULL,
    IdSimulado INTEGER,
    DtRealizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_simuladosrealizacoes_usuario
        FOREIGN KEY (IdUsuario) REFERENCES Usuarios (IdUsuario) ON DELETE CASCADE,
    CONSTRAINT fk_simuladosrealizacoes_simulado
        FOREIGN KEY (IdSimulado) REFERENCES Simulados (IdSimulado) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS ix_simuladosrealizacoes_idusuario
    ON SimuladosRealizacoes (IdUsuario);
