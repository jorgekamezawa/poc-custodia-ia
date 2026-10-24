CREATE TABLE contract (
    id                   UUID          PRIMARY KEY,
    number               VARCHAR(30)   NOT NULL,
    customer_name        VARCHAR(120)  NOT NULL,
    customer_cpf         CHAR(11)      NOT NULL,
    chassis_number       CHAR(17)      NOT NULL,
    vehicle_description  VARCHAR(120)  NOT NULL,
    amortization_system  VARCHAR(10)   NOT NULL CHECK (amortization_system IN ('PRICE', 'SAC')),
    principal            NUMERIC(15,2) NOT NULL,
    monthly_rate_percent NUMERIC(7,4)  NOT NULL,
    term_months          INTEGER       NOT NULL,
    release_date         DATE          NOT NULL,
    first_due_date       DATE          NOT NULL,
    upfront_costs        NUMERIC(15,2) NOT NULL,
    annual_cet_percent   NUMERIC(9,2)  NOT NULL,
    status               VARCHAR(20)   NOT NULL CHECK (status IN ('ACTIVE')),
    registered_by        VARCHAR(60)   NOT NULL,
    registered_at        TIMESTAMPTZ   NOT NULL
);

CREATE UNIQUE INDEX contract_number_key ON contract (number);

CREATE TABLE contract_installment (
    contract_id   UUID          NOT NULL REFERENCES contract (id),
    number        INTEGER       NOT NULL,
    due_date      DATE          NOT NULL,
    amount        NUMERIC(15,2) NOT NULL,
    interest      NUMERIC(15,2) NOT NULL,
    amortization  NUMERIC(15,2) NOT NULL,
    balance_after NUMERIC(15,2) NOT NULL,
    PRIMARY KEY (contract_id, number)
);
