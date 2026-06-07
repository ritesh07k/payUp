CREATE TABLE refunds (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id          UUID NOT NULL REFERENCES payments(id),
    merchant_id         UUID NOT NULL REFERENCES merchants(id),
    amount              BIGINT NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    idempotency_key     VARCHAR(255) NOT NULL,
    reason              VARCHAR(255),
    bank_reference_id   VARCHAR(255),
    failure_reason      VARCHAR(255),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_refunds_idempotency 
    ON refunds(payment_id, idempotency_key);
CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_merchant_id ON refunds(merchant_id);