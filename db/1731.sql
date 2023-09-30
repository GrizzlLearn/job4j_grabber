CREATE TABLE post (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    text TEXT NOT NULL,
    link TEXT NOT NULL UNIQUE,
    created TIMESTAMP
);
