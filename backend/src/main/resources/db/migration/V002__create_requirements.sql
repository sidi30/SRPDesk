CREATE TABLE requirements (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_ref     VARCHAR(50) NOT NULL UNIQUE,
    title           VARCHAR(500) NOT NULL,
    description     TEXT,
    category        VARCHAR(100) NOT NULL,
    applicable_to   VARCHAR(255) NOT NULL DEFAULT 'DEFAULT,CLASS_I,CLASS_II'
);
