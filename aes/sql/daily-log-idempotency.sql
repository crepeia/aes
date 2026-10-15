-- Review/apply only to an isolated test database first. Not executed by this change.
-- All participating tables must use InnoDB for atomic dose/points/receipt writes.
CREATE TABLE IF NOT EXISTS tb_daily_log_operation (
    operation_key VARCHAR(80) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_json LONGTEXT NOT NULL,
    PRIMARY KEY (operation_key)
) ENGINE=InnoDB;
