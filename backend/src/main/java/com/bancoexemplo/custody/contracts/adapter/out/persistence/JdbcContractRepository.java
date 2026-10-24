package com.bancoexemplo.custody.contracts.adapter.out.persistence;

import com.bancoexemplo.custody.contracts.application.ContractSummary;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.AmortizationSystem;
import com.bancoexemplo.custody.contracts.domain.ChassisNumber;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractInstallment;
import com.bancoexemplo.custody.contracts.domain.ContractNumber;
import com.bancoexemplo.custody.contracts.domain.ContractStatus;
import com.bancoexemplo.custody.contracts.domain.Cpf;
import com.bancoexemplo.custody.contracts.domain.DuplicateContractNumberException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JdbcContractRepository implements ContractRepository {

    private static final String SUMMARY =
            "SELECT id, number, customer_name, customer_cpf, principal, registered_at FROM contract";
    // A typed % or _ is part of the number being searched, not a wildcard.
    private static final Pattern LIKE_SPECIAL = Pattern.compile("[\\\\%_]");

    private final JdbcClient jdbc;

    JdbcContractRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsByNumber(ContractNumber number) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM contract WHERE number = :number)")
                .param("number", number.value())
                .query(Boolean.class)
                .single();
    }

    @Override
    @Transactional
    public void save(Contract contract) {
        try {
            jdbc.sql("""
                            INSERT INTO contract (id, number, customer_name, customer_cpf, chassis_number,
                                vehicle_description, amortization_system, principal, monthly_rate_percent, term_months,
                                release_date, first_due_date, upfront_costs, annual_cet_percent, status, registered_by,
                                registered_at)
                            VALUES (:id, :number, :customerName, :customerCpf, :chassisNumber, :vehicleDescription,
                                :amortizationSystem, :principal, :monthlyRatePercent, :termMonths, :releaseDate,
                                :firstDueDate, :upfrontCosts, :annualCetPercent, :status, :registeredBy, :registeredAt)
                            """)
                    .param("id", contract.id())
                    .param("number", contract.number().value())
                    .param("customerName", contract.customerName())
                    .param("customerCpf", contract.customerCpf().digits())
                    .param("chassisNumber", contract.chassisNumber().value())
                    .param("vehicleDescription", contract.vehicleDescription())
                    .param("amortizationSystem", contract.amortizationSystem().name())
                    .param("principal", contract.principal())
                    .param("monthlyRatePercent", contract.monthlyRatePercent())
                    .param("termMonths", contract.termMonths())
                    .param("releaseDate", contract.releaseDate())
                    .param("firstDueDate", contract.firstDueDate())
                    .param("upfrontCosts", contract.upfrontCosts())
                    .param("annualCetPercent", contract.annualCetPercent())
                    .param("status", contract.status().name())
                    .param("registeredBy", contract.registeredBy())
                    .param("registeredAt", Timestamp.from(contract.registeredAt()))
                    .update();
        } catch (DuplicateKeyException e) {
            throw new DuplicateContractNumberException();
        }
        for (ContractInstallment installment : contract.installments()) {
            jdbc.sql("""
                            INSERT INTO contract_installment (contract_id, number, due_date, amount, interest,
                                amortization, balance_after)
                            VALUES (:contractId, :number, :dueDate, :amount, :interest, :amortization, :balanceAfter)
                            """)
                    .param("contractId", contract.id())
                    .param("number", installment.number())
                    .param("dueDate", installment.dueDate())
                    .param("amount", installment.amount())
                    .param("interest", installment.interest())
                    .param("amortization", installment.amortization())
                    .param("balanceAfter", installment.balanceAfter())
                    .update();
        }
    }

    @Override
    public Optional<Contract> findById(UUID id) {
        List<ContractInstallment> installments = jdbc.sql("""
                        SELECT number, due_date, amount, interest, amortization, balance_after
                        FROM contract_installment WHERE contract_id = :id ORDER BY number
                        """)
                .param("id", id)
                .query((row, index) -> new ContractInstallment(
                        row.getInt("number"),
                        row.getObject("due_date", LocalDate.class),
                        row.getBigDecimal("amount"),
                        row.getBigDecimal("interest"),
                        row.getBigDecimal("amortization"),
                        row.getBigDecimal("balance_after")))
                .list();
        return jdbc.sql("SELECT * FROM contract WHERE id = :id")
                .param("id", id)
                .query((row, index) -> contract(row, installments))
                .optional();
    }

    @Override
    public List<ContractSummary> findLatest(int limit) {
        return jdbc.sql(SUMMARY + " ORDER BY registered_at DESC LIMIT :limit")
                .param("limit", limit)
                .query(JdbcContractRepository::summary)
                .list();
    }

    @Override
    public List<ContractSummary> findByCpf(String digits, int limit) {
        return jdbc.sql(SUMMARY + " WHERE customer_cpf = :cpf ORDER BY registered_at DESC LIMIT :limit")
                .param("cpf", digits)
                .param("limit", limit)
                .query(JdbcContractRepository::summary)
                .list();
    }

    @Override
    public List<ContractSummary> findByNumberPrefix(String prefix, int limit) {
        String pattern = LIKE_SPECIAL.matcher(prefix.toUpperCase(Locale.ROOT)).replaceAll("\\\\$0") + "%";
        return jdbc.sql(SUMMARY
                        + " WHERE upper(number) LIKE :pattern ESCAPE '\\' ORDER BY registered_at DESC LIMIT :limit")
                .param("pattern", pattern)
                .param("limit", limit)
                .query(JdbcContractRepository::summary)
                .list();
    }

    private static ContractSummary summary(ResultSet row, int index) throws SQLException {
        return new ContractSummary(
                row.getObject("id", UUID.class),
                row.getString("number"),
                row.getString("customer_name"),
                row.getString("customer_cpf"),
                row.getBigDecimal("principal"),
                row.getTimestamp("registered_at").toInstant());
    }

    private static Contract contract(ResultSet row, List<ContractInstallment> installments) throws SQLException {
        return new Contract(
                row.getObject("id", UUID.class),
                new ContractNumber(row.getString("number")),
                row.getString("customer_name"),
                new Cpf(row.getString("customer_cpf")),
                new ChassisNumber(row.getString("chassis_number")),
                row.getString("vehicle_description"),
                AmortizationSystem.valueOf(row.getString("amortization_system")),
                row.getBigDecimal("principal"),
                row.getBigDecimal("monthly_rate_percent"),
                row.getInt("term_months"),
                row.getObject("release_date", LocalDate.class),
                row.getObject("first_due_date", LocalDate.class),
                row.getBigDecimal("upfront_costs"),
                row.getBigDecimal("annual_cet_percent"),
                ContractStatus.valueOf(row.getString("status")),
                row.getString("registered_by"),
                row.getTimestamp("registered_at").toInstant(),
                installments);
    }
}
