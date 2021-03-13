CREATE TABLE IF NOT EXISTS posts
(
    id         BIGSERIAL PRIMARY KEY,
    title      CHARACTER VARYING(1000) NOT NULL,
    content    TEXT NOT NULL,
    upvote     INTEGER DEFAULT 0,
    downvote   INTEGER DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
    );
