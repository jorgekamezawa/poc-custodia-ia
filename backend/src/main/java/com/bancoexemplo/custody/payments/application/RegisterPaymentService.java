package com.bancoexemplo.custody.payments.application;

import com.bancoexemplo.custody.payments.application.port.ContractToPay;
import com.bancoexemplo.custody.payments.application.port.PaymentEvents;
import com.bancoexemplo.custody.payments.application.port.PaymentRepository;
import com.bancoexemplo.custody.payments.domain.payment.ContractNotFoundForPaymentException;
import com.bancoexemplo.custody.payments.domain.payment.ContractTerms;
import com.bancoexemplo.custody.payments.domain.payment.InstallmentTerms;
import com.bancoexemplo.custody.payments.domain.payment.LateCharges;
import com.bancoexemplo.custody.payments.domain.payment.Payment;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterPaymentService {

    private final ContractToPay contracts;
    private final PaymentRepository payments;
    private final PaymentEvents events;
    private final Clock clock;

    public RegisterPaymentService(
            ContractToPay contracts, PaymentRepository payments, PaymentEvents events, Clock clock) {
        this.contracts = contracts;
        this.payments = payments;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public Payment register(RegisterPaymentCommand command) {
        Payment payment = Payment.create(
                command.contractId(),
                terms(command.contractId()),
                payments.paidInstallments(command.contractId()),
                command.installmentNumber(),
                command.paidOn(),
                command.amount(),
                command.registeredBy(),
                today(),
                Instant.now(clock));
        payments.save(payment);
        events.paymentRegistered(payment);
        return payment;
    }

    /** The amount due for the installment on the date, with the same refusals as registering it. */
    public PaymentQuote quote(UUID contractId, int installmentNumber, LocalDate paidOn) {
        InstallmentTerms installment =
                terms(contractId).payable(installmentNumber, paidOn, payments.paidInstallments(contractId), today());
        LateCharges due = LateCharges.of(installment.amount(), installment.dueDate(), paidOn);
        return new PaymentQuote(
                installmentNumber,
                installment.amount(),
                due.daysLate(),
                due.lateFee(),
                due.defaultInterest(),
                due.total());
    }

    private ContractTerms terms(UUID contractId) {
        return contracts.find(contractId).orElseThrow(ContractNotFoundForPaymentException::new);
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
