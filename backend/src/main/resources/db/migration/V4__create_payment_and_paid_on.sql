CREATE TABLE payment (
    id                 UUID          PRIMARY KEY,
    contract_id        UUID          NOT NULL,
    installment_number INTEGER       NOT NULL,
    paid_on            DATE          NOT NULL,
    amount             NUMERIC(15,2) NOT NULL,
    registered_by      VARCHAR(60)   NOT NULL,
    registered_at      TIMESTAMPTZ   NOT NULL
);

CREATE UNIQUE INDEX payment_contract_installment_key ON payment (contract_id, installment_number);

ALTER TABLE contract_installment ADD COLUMN paid_on DATE;
