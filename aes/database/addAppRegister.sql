--para criar a coluna sem o Hibernate
ALTER TABLE tb_user
ADD COLUMN app_signup BOOLEAN DEFAULT FALSE;

--para alterar a coluna se ela for criada pelo Hibernate
UPDATE aes.tb_user SET app_signup=FALSE
WHERE app_signup IS NULL

--para me colocar como consultor
--UPDATE aes.tb_user SET is_consultant=TRUE
--WHERE email="brunomarcosps@hotmail.com"