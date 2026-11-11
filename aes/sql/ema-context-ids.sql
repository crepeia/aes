-- Additive only: existing answers/texts are never rewritten or deleted.
-- Apply BEFORE deploying the backend, or verify that Hibernate auto-update
-- already added BOTH nullable SMALLINT columns. Apply only missing additions
-- if either column already exists; do not rerun this ALTER blindly.
ALTER TABLE tb_ema_answers
    ADD COLUMN company_id SMALLINT NULL,
    ADD COLUMN current_location_id SMALLINT NULL;

-- Stable, independent dictionaries (never renumber/reuse IDs):
-- ID | company_id          | current_location_id
--  1 | Alone               | Home
--  2 | Friends             | Work/School
--  3 | Partner             | Bar/Restaurant/Club
--  4 | Family              | Party (Someone else's house)
--  5 | Coworkers           | Transit/Street
--  6 | Others              | Other
