-- Retorno das respostas do AUDIT de todos os usuários identificados por ID
SELECT 
id,audit_1,audit_2,audit_3,audit_4,audit_5,
audit_6,audit_7,audit_8,audit_9,audit_10 
FROM tb_evaluation;
-- Retorno das respostas do AUDIT de todos os usuários identificados por email
SELECT 
u.email,e.audit_1,e.audit_2,e.audit_3,e.audit_4,e.audit_5,
e.audit_6,e.audit_7,e.audit_8,e.audit_9,e.audit_10 
FROM tb_user AS u
INNER JOIN tb_evaluation AS e 
ON u.id = e.id;
-- Retorno das respostas do AUDIT de apenas um usuário identificado por ID
SELECT 
id,audit_1,audit_2,audit_3,audit_4,audit_5,
audit_6,audit_7,audit_8,audit_9,audit_10 
FROM tb_evaluation 
WHERE id = 1;
-- Retorno das respostas do AUDIT de apenas um usuário identificado por email, filtrando por id
SELECT 
u.email,e.audit_1,e.audit_2,e.audit_3,e.audit_4,e.audit_5,
e.audit_6,e.audit_7,e.audit_8,e.audit_9,e.audit_10 
FROM tb_user AS u
INNER JOIN tb_evaluation AS e 
ON u.id = e.id
WHERE u.id = 1;
-- Retorno das respostas do AUDIT de apenas um usuário identificado por email, filtrando por email
SELECT 
u.email,e.audit_1,e.audit_2,e.audit_3,e.audit_4,e.audit_5,
e.audit_6,e.audit_7,e.audit_8,e.audit_9,e.audit_10 
FROM tb_user AS u
INNER JOIN tb_evaluation AS e 
ON u.id = e.id
WHERE u.email = 'teste@email.com';
-- Retorno das respostas do AUDIT de apenas um usuário identificado por id, filtrando por id
SELECT 
u.id,e.audit_1,e.audit_2,e.audit_3,e.audit_4,e.audit_5,
e.audit_6,e.audit_7,e.audit_8,e.audit_9,e.audit_10 
FROM tb_user AS u
INNER JOIN tb_evaluation AS e 
ON u.id = e.id
WHERE u.id = 1;
-- Retorno das respostas do AUDIT de apenas um usuário identificado por id, filtrando por email
SELECT 
u.id,e.audit_1,e.audit_2,e.audit_3,e.audit_4,e.audit_5,
e.audit_6,e.audit_7,e.audit_8,e.audit_9,e.audit_10 
FROM tb_user AS u
INNER JOIN tb_evaluation AS e 
ON u.id = e.id
WHERE u.email = 'teste@email.com';