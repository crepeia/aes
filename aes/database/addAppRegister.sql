--para criar a coluna sem o Hibernate
ALTER TABLE tb_user
ADD COLUMN app_signup BOOLEAN DEFAULT FALSE;

--para alterar a coluna se ela for criada pelo Hibernate
UPDATE aes.tb_user SET app_signup=FALSE
WHERE app_signup IS NULL

