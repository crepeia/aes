-- Review on an isolated test database first. This script is not run by the app.
-- tb_user, tb_ema_answers and this table must use InnoDB.
CREATE TABLE IF NOT EXISTS tb_ema_operation (
    operation_key VARCHAR(80) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_json LONGTEXT NOT NULL,
    PRIMARY KEY (operation_key)
) ENGINE=InnoDB;
