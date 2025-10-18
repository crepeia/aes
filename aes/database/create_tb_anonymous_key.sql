/**
 * Author:  luansb
 * Created: 01/10/2025
 */

CREATE TABLE tb_anonymous_key (
    id SERIAL PRIMARY KEY,
    instance_id VARCHAR(64) NOT NULL, -- gerado pelo app
    public_key TEXT NOT NULL,
    client_meta VARCHAR(64), -- opcional
    date_created TIMESTAMP NOT NULL DEFAULT now(),
    revoked BOOLEAN NOT NULL DEFAULT false
);
