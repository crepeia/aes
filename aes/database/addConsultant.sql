--para criar a coluna sem o Hibernate
ALTER TABLE tb_user
ADD COLUMN is_consultant BOOLEAN DEFAULT FALSE;

--para alterar a coluna se ela for criada pelo Hibernate
UPDATE aes.tb_user SET is_consultant=FALSE
WHERE is_consultant IS NULL
