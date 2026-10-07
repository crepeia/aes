-- Review/test on an isolated database, then apply BEFORE deploying the new backend.
-- Additive only: never infer missing time zones or rewrite historical timestamps.
-- If schema auto-update already created any of these columns, inspect the schema
-- and apply only the missing additions instead of running this statement again.
ALTER TABLE tb_ema_answers
    ADD COLUMN captured_at_utc_ms BIGINT NULL,
    ADD COLUMN device_time_zone VARCHAR(64) NULL,
    ADD COLUMN device_utc_offset_minutes INT NULL,
    ADD COLUMN received_at_utc_ms BIGINT NULL;
