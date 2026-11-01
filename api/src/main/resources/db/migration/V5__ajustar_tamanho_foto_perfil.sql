-- Amplia coluna de foto de perfil para suportar URLs longas e imagens em Base64 (alinhado com a entidade User)
ALTER TABLE tb_user ALTER COLUMN profile_picture_url SET DATA TYPE VARCHAR(10485760);
