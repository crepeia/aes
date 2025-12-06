/**
 * Author:  luansb
 * Created: 11/11/2025
 */

/* 
Plano de Exeçução Para "Colapsar" Chats de Usuários Anônimos em Chats "Comuns" 

- Faz um backup completo antes;
- Roda os scripts de forma incremental e reversível;
- Confere os dados antes de aplicar o merge definitivo;
- Remove a coluna unauthenticated_id ao final.
*/

/* BACKUP E PREPRAÇÃO */
-- Backup completo das três tabelas principais
-- CREATE TABLE tb_user_backup LIKE tb_user;
-- INSERT INTO tb_user_backup SELECT * FROM tb_user;
-- 
-- CREATE TABLE tb_chat_backup LIKE tb_chat;
-- INSERT INTO tb_chat_backup SELECT * FROM tb_chat;
-- 
-- CREATE TABLE tb_message_backup LIKE tb_message;
-- INSERT INTO tb_message_backup SELECT * FROM tb_message;

-- Cria tabela de logs para acompanhar a migração
CREATE TABLE IF NOT EXISTS migration_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unauthenticated_id VARCHAR(255),
    old_chat_id BIGINT,
    new_chat_id BIGINT,
    new_user_id BIGINT,
    action VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

/* CRIA USUÁRIOS PARA CADA unauthenticated_id */
-- Varre todos os unauthenticated_id existentes e garante que cada um tenha um usuário correspondente em tb_user

-- Preview: ver os unauthenticated_id únicos que ainda não existem em tb_user
SELECT DISTINCT c.unauthenticated_id
FROM tb_chat c
LEFT JOIN tb_user u ON u.unauthenticated_id = c.unauthenticated_id
WHERE c.unauthenticated_id IS NOT NULL
  AND u.id IS NULL;

-- Inserção efetiva: cria um user para cada unauthenticated_id não presente
INSERT INTO tb_user (
    is_admin,
    app_signup,
    authorize_data,
    is_consultant,
    date_created,
    in_ranking,
    name,
    registration_complete,
    sign_up_date,
    use_chatbot,
    unauthenticated_id
)
SELECT
    FALSE,                                   -- is_admin
    FALSE,                                   -- app_signup
    FALSE,                                   -- authorize_data
    FALSE,                                   -- is_consultant
    NOW(),                                   -- date_created
    FALSE,                                   -- in_ranking
    'Anônimo',                               -- name
    TRUE,                                    -- registration_complete
    NOW(),                                   -- sign_up_date
    FALSE,                                   -- use_chatbot
    t.unauthenticated_id                     -- unauthenticated_id
FROM (
    SELECT DISTINCT unauthenticated_id
    FROM tb_chat
    WHERE unauthenticated_id IS NOT NULL
) t
LEFT JOIN tb_user u ON u.unauthenticated_id = t.unauthenticated_id
WHERE u.id IS NULL;

/* CRIAR O CHAT CORRESPONDENTE A CADA ANÔNIMO */
-- Preview: qual seria o start_date escolhido por unauthenticated_id
SELECT c.unauthenticated_id, MIN(c.start_date) AS first_start_date, COUNT(*) AS qtd_chats
FROM tb_chat c
WHERE c.unauthenticated_id IS NOT NULL
GROUP BY c.unauthenticated_id;

-- Cria um único chat consolidado por usuário anônimo
INSERT INTO tb_chat (user_id, start_date)
SELECT u.id, t.first_start_date
FROM (
    SELECT unauthenticated_id, MIN(start_date) AS first_start_date
    FROM tb_chat
    WHERE unauthenticated_id IS NOT NULL
    GROUP BY unauthenticated_id
) t
JOIN tb_user u ON u.unauthenticated_id = t.unauthenticated_id
LEFT JOIN tb_chat existing ON existing.user_id = u.id
WHERE existing.id IS NULL;

/* MAPEAR OS CHATS ANTIGOS PARA O NOVO CHAT */
DROP TABLE IF EXISTS chat_merge_map;

-- Cria uma tabela auxiliar com o mapeamento entre unauthenticated_id, old_chat_id e new_chat_id
CREATE TABLE chat_merge_map AS
SELECT 
    c.id AS old_chat_id,
    c.unauthenticated_id,
    u.id AS new_user_id,
    nc.id AS new_chat_id
FROM tb_chat c
JOIN tb_user u ON u.unauthenticated_id = c.unauthenticated_id
JOIN tb_chat nc ON nc.user_id = u.id
WHERE c.unauthenticated_id IS NOT NULL;

-- Verifica conteúdo
SELECT COUNT(*) AS mappings, MIN(old_chat_id) AS min_old, MAX(old_chat_id) AS max_old
FROM chat_merge_map;
SELECT * FROM chat_merge_map LIMIT 50;

/* MIGRAR AS MENSAGENS */

SET FOREIGN_KEY_CHECKS = 0;

-- Preview: ver quantas mensagens serão afetadas
SELECT map.old_chat_id, map.new_chat_id, COUNT(m.id) AS mensagens_afetadas
FROM chat_merge_map map
LEFT JOIN tb_message m ON m.chat_id = map.old_chat_id
GROUP BY map.old_chat_id, map.new_chat_id;

-- Executar o UPDATE
START TRANSACTION;

UPDATE tb_message m
JOIN chat_merge_map map ON m.chat_id = map.old_chat_id
SET m.chat_id = map.new_chat_id,
    m.id_from = map.new_user_id;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- Mensagens agora apontando para os novos chats
SELECT m.id, m.chat_id, m.id_from, m.name_from, m.sent_date
FROM tb_message m
JOIN chat_merge_map map ON m.chat_id = map.new_chat_id
LIMIT 50;

/* LOGAR O QUE FOI MIGRADO */
INSERT INTO migration_log (unauthenticated_id, old_chat_id, new_chat_id, new_user_id, action)
SELECT unauthenticated_id, old_chat_id, new_chat_id, new_user_id, 'MERGE_CHAT'
FROM chat_merge_map;

/* VERIFICAÇÃO ANTES DE REMOVER */
-- Quantidade de mensagens antes vs depois (comparação básica)
SELECT COUNT(*) FROM tb_message;
SELECT COUNT(*) FROM tb_message WHERE chat_id IN (SELECT old_chat_id FROM chat_merge_map); -- deve ser 0

-- Verificar se ainda há chats com unauthenticated_id
SELECT COUNT(*) AS chats_with_unauth FROM tb_chat WHERE unauthenticated_id IS NOT NULL;

-- Verificar mensagens que ainda referenciam chats antigos (se algo deu errado)
SELECT m.*
FROM tb_message m
LEFT JOIN chat_merge_map map ON m.chat_id = map.old_chat_id
WHERE map.old_chat_id IS NOT NULL
LIMIT 50;

/* LIMPEZA FINAL */
-- Apagar chats antigos que tinham unauthenticated_id (agora consolidados)
DELETE c FROM tb_chat c
WHERE c.unauthenticated_id IS NOT NULL
  AND c.id NOT IN (SELECT DISTINCT new_chat_id FROM chat_merge_map);

-- Confirmar que não restaram chats com unauthenticated_id
SELECT COUNT(*) FROM tb_chat WHERE unauthenticated_id IS NOT NULL;

-- Quando tudo estiver testado:
ALTER TABLE tb_chat DROP COLUMN unauthenticated_id;

/* ROLLBACK (CASO ALGO DÊ ERRADO) */
-- SET FOREIGN_KEY_CHECKS = 0;
-- 
-- -- Restaura tabelas originais
-- TRUNCATE TABLE tb_message;
-- TRUNCATE TABLE tb_chat;
-- TRUNCATE TABLE tb_user;
-- 
-- INSERT INTO tb_user SELECT * FROM tb_user_backup;
-- INSERT INTO tb_chat SELECT * FROM tb_chat_backup;
-- INSERT INTO tb_message SELECT * FROM tb_message_backup;
-- 
-- SET FOREIGN_KEY_CHECKS = 1;