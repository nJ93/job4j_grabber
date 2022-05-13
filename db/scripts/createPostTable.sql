CREATE TABLE IF NOT EXISTS post (
    id SERIAL PRIMARY KEY,
    title TEXT,
    link TEXT UNIQUE NOT NULL,
    description TEXT,
    created TIMESTAMP
);