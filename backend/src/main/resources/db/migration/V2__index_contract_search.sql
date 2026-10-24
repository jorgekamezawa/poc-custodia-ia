CREATE INDEX contract_customer_cpf_idx ON contract (customer_cpf);
CREATE INDEX contract_registered_at_idx ON contract (registered_at);
CREATE INDEX contract_number_upper_idx ON contract (upper(number) text_pattern_ops);
