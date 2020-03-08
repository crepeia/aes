--para criar a coluna sem o Hibernate
ALTER TABLE tb_user
ADD COLUMN is_admin BOOLEAN DEFAULT FALSE;

--para alterar a coluna se ela for criada pelo Hibernate
UPDATE aes.tb_user SET is_admin=FALSE
WHERE is_admin IS NULL

--para me colocar como admin
UPDATE aes.tb_user SET is_admin=TRUE
WHERE email="brunomarcosps@hotmail.com"