CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO team (id, created_at, updated_at, name)
VALUES
    (gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'CARDS'),
    (gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'BORROW'),
    (gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'OTHERS')
ON CONFLICT (name) DO NOTHING;
