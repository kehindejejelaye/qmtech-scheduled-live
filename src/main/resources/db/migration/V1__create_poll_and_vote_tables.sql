-- V1__create_poll_and_vote_tables.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS poll (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    question text NOT NULL,
    options jsonb NOT NULL,
    scheduled_start_time timestamptz NOT NULL,
    created_at timestamptz DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS vote (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    poll_id uuid NOT NULL REFERENCES poll(id) ON DELETE CASCADE,
    username varchar NOT NULL,
    option_chosen text NOT NULL,
    created_at timestamptz DEFAULT now()
    );

CREATE INDEX idx_vote_poll_id ON vote(poll_id);
