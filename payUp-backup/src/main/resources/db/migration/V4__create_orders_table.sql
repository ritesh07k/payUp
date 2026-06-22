CREATE TABLE orders (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id     UUID NOT NULL REFERENCES merchants(id),
    amount          BIGINT NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'INR',
    receipt         VARCHAR(255),
    status          VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_merchant_id ON orders(merchant_id);