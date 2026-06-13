CREATE TABLE ledger_entries (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id     UUID NOT NULL REFERENCES merchants(id),
    entry_type      VARCHAR(10) NOT NULL,
    amount          BIGINT NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    reference_type  VARCHAR(20) NOT NULL,
    reference_id    UUID NOT NULL,
    description     VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ledger_merchant_id ON ledger_entries(merchant_id);
CREATE INDEX idx_ledger_reference ON ledger_entries(reference_type, reference_id);