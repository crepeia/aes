/**
 * Author:  luansb
 * Created: 30/10/2025
 */

ALTER TABLE tb_user ADD COLUMN unauthenticated_id VARCHAR(64) UNIQUE DEFAULT NULL

ALTER TABLE tb_user DROP COLUMN advanced_data_consent