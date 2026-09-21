CREATE TABLE IF NOT EXISTS clients (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(120) NOT NULL,
    birth DATE NOT NULL,
    cpf VARCHAR(14) NOT NULL,
    email VARCHAR(160) NOT NULL,
    phone VARCHAR(30),
    password_hash VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    rank_value INT NOT NULL DEFAULT 0,
    street VARCHAR(120),
    address_number VARCHAR(20),
    city VARCHAR(80),
    state VARCHAR(2),
    PRIMARY KEY (id),
    UNIQUE KEY uk_clients_code (code),
    UNIQUE KEY uk_clients_cpf (cpf),
    UNIQUE KEY uk_clients_email (email)
);

CREATE TABLE IF NOT EXISTS client_addresses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    client_id BIGINT NOT NULL,
    label VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    residence_type VARCHAR(60) NOT NULL,
    street_type VARCHAR(40) NOT NULL,
    street VARCHAR(120) NOT NULL,
    number VARCHAR(20) NOT NULL,
    neighborhood VARCHAR(80) NOT NULL,
    cep VARCHAR(9) NOT NULL,
    city VARCHAR(80) NOT NULL,
    state VARCHAR(2) NOT NULL,
    country VARCHAR(80) NOT NULL,
    notes VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_client_addresses_client FOREIGN KEY (client_id) REFERENCES clients (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS card_brands (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(40) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_card_brands_name (name)
);

INSERT IGNORE INTO card_brands (name) VALUES
    ('American Express'), ('Elo'), ('Hipercard'), ('Mastercard'), ('Visa');

CREATE TABLE IF NOT EXISTS client_cards (
    id BIGINT NOT NULL AUTO_INCREMENT,
    client_id BIGINT NOT NULL,
    brand_id BIGINT NOT NULL,
    card_number_hash VARCHAR(100) NOT NULL,
    last_four VARCHAR(4) NOT NULL,
    holder_name VARCHAR(120) NOT NULL,
    security_code_hash VARCHAR(100) NOT NULL,
    preferred BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT fk_client_cards_client FOREIGN KEY (client_id) REFERENCES clients (id) ON DELETE CASCADE,
    CONSTRAINT fk_client_cards_brand FOREIGN KEY (brand_id) REFERENCES card_brands (id)
);
