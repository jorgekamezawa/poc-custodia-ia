package com.bancoexemplo.custody.contracts.adapter.out.persistence;

import com.bancoexemplo.custody.contracts.domain.contract.AmortizationSystem;
import com.bancoexemplo.custody.contracts.domain.contract.ContractStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A row of {@code contract}, with its installments; the domain never sees this class. */
@Entity
@Table(name = "contract")
class ContractJpaEntity {

    @Id
    UUID id;

    String number;

    @Column(name = "customer_name")
    String customerName;

    @Column(name = "customer_cpf")
    @JdbcTypeCode(SqlTypes.CHAR)
    String customerCpf;

    @Column(name = "chassis_number")
    @JdbcTypeCode(SqlTypes.CHAR)
    String chassisNumber;

    @Column(name = "vehicle_description")
    String vehicleDescription;

    @Column(name = "amortization_system")
    @Enumerated(EnumType.STRING)
    AmortizationSystem amortizationSystem;

    BigDecimal principal;

    @Column(name = "monthly_rate_percent")
    BigDecimal monthlyRatePercent;

    @Column(name = "term_months")
    int termMonths;

    @Column(name = "release_date")
    LocalDate releaseDate;

    @Column(name = "first_due_date")
    LocalDate firstDueDate;

    @Column(name = "upfront_costs")
    BigDecimal upfrontCosts;

    @Column(name = "annual_cet_percent")
    BigDecimal annualCetPercent;

    @Enumerated(EnumType.STRING)
    ContractStatus status;

    @Column(name = "registered_by")
    String registeredBy;

    @Column(name = "registered_at")
    Instant registeredAt;

    // The installment writes its own contract_id as part of its key, so this side only reads the column.
    @OneToMany(cascade = jakarta.persistence.CascadeType.PERSIST)
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    @OrderBy("number")
    List<ContractInstallmentJpaEntity> installments = new ArrayList<>();
}
