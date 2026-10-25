package com.bancoexemplo.custody.payments.domain.payment;

import com.bancoexemplo.custody.payments.domain.payment.exception.InvalidPaymentException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** A registered payment of one installment; the amount includes the late fee and the default interest. */
public final class Payment {

    private final UUID id;
    private final UUID contractId;
    private final int installmentNumber;
    private final LocalDate paidOn;
    private final BigDecimal amount;
    private final BigDecimal lateFee;
    private final BigDecimal defaultInterest;
    private final String registeredBy;
    private final Instant registeredAt;

    private Payment(
            UUID id,
            UUID contractId,
            int installmentNumber,
            LocalDate paidOn,
            BigDecimal amount,
            BigDecimal lateFee,
            BigDecimal defaultInterest,
            String registeredBy,
            Instant registeredAt) {
        this.id = id;
        this.contractId = contractId;
        this.installmentNumber = installmentNumber;
        this.paidOn = paidOn;
        this.amount = amount;
        this.lateFee = lateFee;
        this.defaultInterest = defaultInterest;
        this.registeredBy = registeredBy;
        this.registeredAt = registeredAt;
    }

    /**
     * Registers the payment of an installment: the oldest unpaid one, on a date between the release and today, for
     * exactly what is due on that date.
     *
     * @param paid numbers of the contract's installments already paid
     * @param today the business date in Brasília
     */
    public static Payment create(
            UUID contractId,
            ContractTerms terms,
            Set<Integer> paid,
            Integer installmentNumber,
            LocalDate paidOn,
            BigDecimal amount,
            String registeredBy,
            LocalDate today,
            Instant registeredAt) {
        InstallmentTerms installment = terms.payable(installmentNumber, paidOn, paid, today);
        LateCharges due = LateCharges.of(installment.amount(), installment.dueDate(), paidOn);
        if (amount == null) {
            throw new InvalidPaymentException("Valor é obrigatório");
        }
        if (amount.compareTo(due.total()) != 0) {
            throw new InvalidPaymentException((due.late()
                            ? "Valor deve ser igual ao da parcela com multa e juros de mora: R$ "
                            : "Valor deve ser igual ao da parcela: R$ ")
                    + reais(due.total()));
        }
        return new Payment(
                UUID.randomUUID(),
                Objects.requireNonNull(contractId),
                installment.number(),
                paidOn,
                amount,
                due.lateFee(),
                due.defaultInterest(),
                Objects.requireNonNull(registeredBy),
                Objects.requireNonNull(registeredAt));
    }

    private static String reais(BigDecimal amount) {
        return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.of("pt", "BR"))).format(amount);
    }

    public UUID getId() {
        return id;
    }

    public UUID getContractId() {
        return contractId;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public LocalDate getPaidOn() {
        return paidOn;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public BigDecimal getDefaultInterest() {
        return defaultInterest;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Payment payment && payment.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
