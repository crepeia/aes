--para criar a coluna sem o Hibernate
ALTER TABLE aes.tb_user
ADD COLUMN registration_complete BOOLEAN DEFAULT FALSE;

UPDATE aes.tb_user SET registration_complete=TRUE
WHERE email IS NOT NULL


--para alterar a coluna se ela for criada pelo Hibernate
UPDATE aes.tb_user SET registration_complete=TRUE
WHERE email IS NOT NULL

UPDATE aes.tb_user SET registration_complete=FALSE
WHERE email IS NULL

