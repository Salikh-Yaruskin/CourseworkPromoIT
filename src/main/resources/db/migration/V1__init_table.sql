CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    gmail VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL,
    role VARCHAR NOT NULL,
    status VARCHAR NOT NULL,
    limit_news INTEGER
);

CREATE TABLE news (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    user_id INT NOT NULL,
    view_count INT NOT NULL,
    status VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_view(
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    news_id INT NOT NULL,
    CONSTRAINT fk_news FOREIGN KEY (news_id) REFERENCES news(id)
);