--para criar a coluna sem o Hibernate
ALTER TABLE tb_user
ADD COLUMN use_chatbot BOOLEAN DEFAULT FALSE;

--para alterar a coluna se ela for criada pelo Hibernate
UPDATE aes.tb_user SET use_chatbot=FALSE
WHERE use_chatbot IS NULL

