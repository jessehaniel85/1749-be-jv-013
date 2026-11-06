-- Migration V2: refresh tokens emitidos.
--
-- O refresh token é um JWT, mas guardamos no banco o identificador dele (o claim "jti").
-- Isso dá o que o JWT sozinho não tem: poder REVOGAR (logout) e detectar REUSO.
-- O banco não guarda o token inteiro, só o jti: quem lê a tabela não consegue usar nada dali.

CREATE TABLE refresh_tokens (
    id         VARCHAR(36) PRIMARY KEY,          -- o jti (um UUID)
    usuario_id BIGINT      NOT NULL,
    expira_em  TIMESTAMP WITH TIME ZONE NOT NULL,
    -- revogado = TRUE: já foi usado (rotação) ou o usuário fez logout
    revogado   BOOLEAN     NOT NULL DEFAULT FALSE,
    criado_em  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE INDEX ix_refresh_tokens_usuario ON refresh_tokens (usuario_id);
