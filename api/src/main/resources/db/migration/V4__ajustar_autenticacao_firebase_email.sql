-- 1. Tornar telefone opcional para usuários autenticados via Conta Google / Firebase
ALTER TABLE tb_user ALTER COLUMN phone DROP NOT NULL;

-- 2. Garantir coluna de senha opcional e campos de integração com o Firebase Authentication
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS password VARCHAR(255);
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS firebase_uid VARCHAR(255);
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(50);
ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE;

-- 3. Remover tabela de tokens de SMS mockado caso exista
DROP TABLE IF EXISTS tb_verification_token;
