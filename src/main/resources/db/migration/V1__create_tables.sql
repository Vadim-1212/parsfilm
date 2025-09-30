CREATE TABLE IF NOT EXISTS films
(
    id                BIGSERIAL PRIMARY KEY,
    kinopoisk_id      BIGINT UNIQUE NOT NULL,
    name_ru           VARCHAR(255),
    name_en           VARCHAR(255),
    name_original     VARCHAR(255),
    poster_url        VARCHAR(500),
    web_url           VARCHAR(500),
    release_year      INT,
    description       TEXT,
    film_types        VARCHAR(50),
    rating_age_limits INT,
    rating_kinopoisk  DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS genres
(
    id         BIGSERIAL PRIMARY KEY,
    genre_name VARCHAR(255) NOT NULL,
    id_api     INT
);

CREATE TABLE IF NOT EXISTS countries
(
    id           BIGSERIAL PRIMARY KEY,
    country_name VARCHAR(255) NOT NULL,
    id_api       INT
);

CREATE TABLE IF NOT EXISTS film_genres
(
    film_id  BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    CONSTRAINT fk_film FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    CONSTRAINT fk_genre FOREIGN KEY (genre_id) REFERENCES genres (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS film_countries
(
    film_id    BIGINT NOT NULL,
    country_id BIGINT NOT NULL,
    PRIMARY KEY (film_id, country_id),
    CONSTRAINT fk_film FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    CONSTRAINT fk_country FOREIGN KEY (country_id) REFERENCES countries (id) ON DELETE CASCADE
);

CREATE TABLE request_counter
(
    id           BIGSERIAL PRIMARY KEY,
    request_date DATE NOT NULL UNIQUE,
    requests     INT  NOT NULL DEFAULT 0
);

