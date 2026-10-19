-- Adiciona a coluna 'cep' à tabela locations (caso não exista)
ALTER TABLE locations ADD COLUMN IF NOT EXISTS cep VARCHAR(20);

-- Insere locais de retirada iniciais para o sistema funcionar
INSERT INTO locations (name, address, cep)
SELECT 'Loja', 'Loja', '00000-000'
WHERE NOT EXISTS (SELECT 1 FROM locations WHERE name = 'Loja');

INSERT INTO locations (name, address, cep)
SELECT 'Casa de um colega', 'Casa de um colega', '00000-000'
WHERE NOT EXISTS (SELECT 1 FROM locations WHERE name = 'Casa de um colega');

INSERT INTO locations (name, address, cep)
SELECT 'Outro local', 'Outro local', '00000-000'
WHERE NOT EXISTS (SELECT 1 FROM locations WHERE name = 'Outro local');
