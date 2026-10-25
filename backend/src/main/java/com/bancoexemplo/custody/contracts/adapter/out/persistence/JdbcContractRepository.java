package com.bancoexemplo.custody.contracts.adapter.out.persistence;

import com.bancoexemplo.custody.contracts.application.ContractSummary;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.contract.AmortizationSystem;
import com.bancoexemplo.custody.contracts.domain.contract.ChassisNumber;
import com.bancoexemplo.custody.contracts.domain.contract.Contract;
import com.bancoexemplo.custody.contracts.domain.contract.ContractInstallment;
import com.bancoexemplo.custody.contracts.domain.contract.ContractNumber;
import com.bancoexemplo.custody.contracts.domain.contract.ContractStatus;
import com.bancoexemplo.custody.contracts.domain.contract.Cpf;
import com.bancoexemplo.custody.contracts.domain.contract.CustomerName;
import com.bancoexemplo.custody.contracts.domain.contract.FinancingTerms;
import com.bancoexemplo.custody.contracts.domain.contract.VehicleDescription;
import com.bancoexemplo.custody.contracts.domain.contract.exception.DuplicateContractNumberException;
import com.bancoexemplo.custody.contracts.domain.contract.exception.InstallmentNotPayableException;
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
                    .param("id", contract.getId())
                    .param("number", contract.getNumber().value())
                    .param("customerName", contract.getCustomerName().value())
                    .param("customerCpf", contract.getCustomerCpf().digits())
                    .param("chassisNumber", contract.getChassisNumber().value())
                    .param(
                            "vehicleDescription",
                            contract.getVehicleDescription().value())
                    .param(
                            "amortizationSystem",
                            contract.getTerms().amortizationSystem().name())
                    .param("principal", contract.getTerms().principal())
                    .param("monthlyRatePercent", contract.getTerms().monthlyRatePercent())
                    .param("termMonths", contract.getTerms().termMonths())
                    .param("releaseDate", contract.getTerms().releaseDate())
                    .param("firstDueDate", contract.getTerms().firstDueDate())
                    .param("upfrontCosts", contract.getTerms().upfrontCosts())
                    .param("annualCetPercent", contract.getAnnualCetPercent())
                    .param("status", contract.getStatus().name())
                    .param("registeredBy", contract.getRegisteredBy())
                    .param("registeredAt", Timestamp.from(contract.getRegisteredAt()))
                    .update();
        } catch (DuplicateKeyException e) {
            throw new DuplicateContractNumberException();
        }
        for (ContractInstallment installment : contract.getInstallments()) {
            jdbc.sql("""
                            INSERT INTO contract_installment (contract_id, number, due_date, amount, interest,
                                amortization, balance_after)
                            VALUES (:contractId, :number, :dueDate, :amount, :interest, :amortization, :balanceAfter)
                            """)
                    .param("contractId", contract.getId())
                    .param("number", installment.getNumber())
                    .param("dueDate", installment.getDueDate())
                    .param("amount", installment.getAmount())
                    .param("interest", installment.getInterest())
                    .param("amortization", installment.getAmortization())
                    .param("balanceAfter", installment.getBalanceAfter())
                    .update();
        }
    }

    @Override
    public Optional<Contract> findById(UUID id) {
        List<ContractInstallment> installments = jdbc.sql("""
                        SELECT number, due_date, amount, interest, amortization, balance_after, paid_on
                        FROM contract_installment WHERE contract_id = :id ORDER BY number
                        """)
                .param("id", id)
                .query((row, index) -> ContractInstallment.reconstruct(
                        row.getInt("number"),
                        row.getObject("due_date", LocalDate.class),
                        row.getBigDecimal("amount"),
                        row.getBigDecimal("interest"),
                        row.getBigDecimal("amortization"),
                        row.getBigDecimal("balance_after"),
                        row.getObject("paid_on", LocalDate.class)))
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

    @Override
    public void markInstallmentPaid(UUID contractId, int installmentNumber, LocalDate paidOn) {
        int updated = jdbc.sql("""
                        UPDATE contract_installment SET paid_on = :paidOn
                        WHERE contract_id = :id AND number = :number AND paid_on IS NULL
                        """)
                .param("paidOn", paidOn)
                .param("id", contractId)
                .param("number", installmentNumber)
                .update();
        if (updated != 1) {
            throw new InstallmentNotPayableException("Parcela " + installmentNumber + " não está em aberto");
        }
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
        return Contract.reconstruct(
                row.getObject("id", UUID.class),
                new ContractNumber(row.getString("number")),
                new CustomerName(row.getString("customer_name")),
                new Cpf(row.getString("customer_cpf")),
                new ChassisNumber(row.getString("chassis_number")),
                new VehicleDescription(row.getString("vehicle_description")),
                new FinancingTerms(
                        AmortizationSystem.valueOf(row.getString("amortization_system")),
                        row.getBigDecimal("principal"),
                        row.getBigDecimal("monthly_rate_percent"),
                        row.getInt("term_months"),
                        row.getObject("release_date", LocalDate.class),
                        row.getObject("first_due_date", LocalDate.class),
                        row.getBigDecimal("upfront_costs")),
                row.getBigDecimal("annual_cet_percent"),
                ContractStatus.valueOf(row.getString("status")),
                row.getString("registered_by"),
                row.getTimestamp("registered_at").toInstant(),
                installments);
    }
}
