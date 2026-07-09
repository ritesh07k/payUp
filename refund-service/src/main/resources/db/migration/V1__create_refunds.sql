CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL,
    merchant_id UUID NOT NULL,
    amount_paise BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
