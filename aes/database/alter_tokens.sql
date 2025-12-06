/**
 * Author:  luansb
 * Created: 01/10/2025
 */

ALTER TABLE tb_authentication_token ADD COLUMN anonymous_key_id INT REFERENCES tb_anonymous_key(id);