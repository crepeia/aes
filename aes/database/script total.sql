--novos campos e alterações
ALTER TABLE tb_user
ADD COLUMN in_ranking BOOLEAN DEFAULT FALSE;

ALTER TABLE tb_user
ADD COLUMN nickname VARCHAR(255) NULL DEFAULT NULL;

ALTER TABLE aes.tb_record
CHANGE COLUMN daily_goal daily_goal FLOAT NULL DEFAULT 0 ,
CHANGE COLUMN weekly_goal weekly_goal FLOAT NULL DEFAULT 0 ;


UPDATE aes.tb_record SET daily_goal=0
WHERE daily_goal IS NULL

UPDATE aes.tb_record SET weekly_goal=0
WHERE weekly_goal IS NULL



--alterar campos criados posteriormente
UPDATE aes.tb_user SET is_consultant=FALSE
WHERE is_consultant IS NULL

UPDATE aes.tb_user SET use_chatbot=FALSE
WHERE use_chatbot IS NULL

UPDATE aes.tb_user SET app_signup=FALSE
WHERE app_signup IS NULL

UPDATE aes.tb_user SET is_admin=FALSE
WHERE is_admin IS NULL

UPDATE aes.tb_user SET registration_complete=TRUE
WHERE email IS NOT NULL

UPDATE aes.tb_user SET registration_complete=FALSE
WHERE email IS NULL

UPDATE aes.tb_user SET in_ranking=FALSE
WHERE in_ranking IS NULL

UPDATE aes.tb_user SET nickname=""
WHERE nickname IS NULL

