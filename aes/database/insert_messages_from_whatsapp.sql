/**
 * Author:  luansb
 * Created: 07/09/2026
 */

START TRANSACTION;
SOURCE insert_tb_message.sql;   -- ou cole os INSERTs

-- confira as mensagens inseridas
SELECT * FROM tb_message
WHERE chat_id IN (58, 91)          -- os chat_id das guias
ORDER BY chat_id, sent_date;

-- se estiver certo:
COMMIT;      

-- senao: ROLLBACK;