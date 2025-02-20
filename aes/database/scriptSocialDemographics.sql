-- Retorno dos dados sócio-demográficos de todos os usuários identificados por id
SELECT 
id, name, birth_date, drink, education, employed, gender, phone, pregnant
FROM tb_user;
-- Retorno dos dados sócio-demográficos de todos os usuários identificados por email
SELECT 
email, name, birth_date, drink, education, employed, gender, phone, pregnant
FROM tb_user;
-- Retorno dos dados sócio-demográficos de apenas um usuário identificado por email e filtrando por id
SELECT 
email, name, birth_date, drink, education, employed, gender, phone, pregnant
FROM tb_user 
WHERE id = 1;
-- Retorno dos dados sócio-demográficos de todos os usuários identificado por email e filtrando por email
SELECT 
email, name, birth_date, drink, education, employed, gender, phone, pregnant
FROM tb_user 
WHERE email = 'teste@email.com';
-- Retorno dos dados sócio-demográficos de todos os usuários identificado por id e filtrando por id
SELECT 
id, name, birth_date, drink, education, employed, gender, phone, pregnant
FROM tb_user 
WHERE id = 1;
-- Retorno dos dados sócio-demográficos de todos os usuários identificado por id e filtrando por email
SELECT 
id, name, birth_date, drink, education, employed, gender, phone, pregnant
FROM tb_user 
WHERE email = 'teste@email.com';