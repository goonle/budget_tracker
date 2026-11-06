CREATE TABLE
    category (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        depth INT NOT NULL,
        parent_id BIGINT REFERENCES category (id),
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

CREATE TABLE
    transaction (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        category_id BIGINT REFERENCES category (id),
        merchant_id BIGINT REFERENCES merchant (id),
        type VARCHAR(20) NOT NULL,
        cent INT NOT NULL,
        date DATE NOT NULL,
        note VARCHAR,
        raw_line VARCHAR NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

CREATE TABLE
    merchant (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR NOT NULL,
        default_category_id BIGINT REFERENCES category (id),
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

CREATE TABLE
    merchant_alias (
        id BIGSERIAL PRIMARY KEY,
        merchant_id BIGINT NOT NULL REFERENCES merchant (id),
        raw_pattern VARCHAR NOT NULL UNIQUE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

CREATE TABLE
    app_user (
        id BIGSERIAL PRIMARY KEY,
        username VARCHAR NOT NULL UNIQUE,
        password_hash VARCHAR NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

CREATE TABLE
    member (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR NOT NULL,
        card_last4 CHAR(4) NOT NULL UNIQUE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );