--para criar as colunas sem o Hibernate
ALTER TABLE tb_user
ADD COLUMN app_signup BOOLEAN DEFAULT FALSE;


ALTER TABLE tb_user
ADD COLUMN use_chatbot BOOLEAN DEFAULT FALSE;


ALTER TABLE aes.tb_user
ADD COLUMN registration_complete BOOLEAN DEFAULT FALSE;

UPDATE aes.tb_user SET registration_complete=TRUE
WHERE email IS NOT NULL



--para alterar as colunas se elas forem criadas pelo Hibernate
UPDATE aes.tb_user SET app_signup=FALSE
WHERE app_signup IS NULL


UPDATE aes.tb_user SET use_chatbot=FALSE
WHERE use_chatbot IS NULL


UPDATE aes.tb_user SET registration_complete=TRUE
WHERE email IS NOT NULL

UPDATE aes.tb_user SET registration_complete=FALSE
WHERE email IS NULL
