CREATE TABLE inventory (
    product_id  BIGINT  NOT NULL PRIMARY KEY,
    quantity    INTEGER NOT NULL,
    version     BIGINT  NOT NULL DEFAULT 0
);

CREATE TABLE reservations (
    idempotency_key  VARCHAR(255) NOT NULL PRIMARY KEY,
    product_id       BIGINT       NOT NULL REFERENCES inventory (product_id),
    quantity_reserved INTEGER     NOT NULL,
    status           VARCHAR(20)  NOT NULL
);
