package com.bancoexemplo.custody.payments.application;

import com.bancoexemplo.custody.payments.application.port.ContractToPay;
import com.bancoexemplo.custody.payments.application.port.ContractToPay.ContractTerms;
import com.bancoexemplo.custody.payments.application.port.ContractToPay.InstallmentTerms;
import com.bancoexemplo.custody.payments.application.port.PaymentEvents;
import com.bancoexemplo.custody.payments.application.port.PaymentRepository;
import com.bancoexemplo.custody.payments.domain.ContractNotFoundForPaymentException;
import com.bancoexemplo.custody.payments.domain.InstallmentAlreadyPaidException;
import com.bancoexemplo.custody.payments.domain.LateCharges;
import com.bancoexemplo.custody.payments.domain.Payment;
import com.bancoexemplo.custody.payments.domain.PaymentRules;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterPaymentService {

    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");

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
        InstallmentTerms installment = payable(command.contractId(), command.installmentNumber(), command.paidOn());
        LateCharges due = LateCharges.of(installment.amount(), installment.dueDate(), command.paidOn());
        PaymentRules.exactAmount(command.amount(), due);
        Payment payment = new Payment(
                UUID.randomUUID(),
                command.contractId(),
                command.installmentNumber(),
                command.paidOn(),
                command.amount(),
                due.lateFee(),
                due.defaultInterest(),
                command.registeredBy(),
                Instant.now(clock));
        payments.save(payment);
        events.paymentRegistered(payment);
        return payment;
    }

    /** The amount due for the installment on the date, with the same refusals as registering it. */
    public PaymentQuote quote(UUID contractId, int installmentNumber, LocalDate paidOn) {
        InstallmentTerms installment = payable(contractId, installmentNumber, paidOn);
        LateCharges due = LateCharges.of(installment.amount(), installment.dueDate(), paidOn);
        return new PaymentQuote(
                installmentNumber,
                installment.amount(),
                due.daysLate(),
                due.lateFee(),
                due.defaultInterest(),
                due.total());
    }

    private InstallmentTerms payable(UUID contractId, int installmentNumber, LocalDate paidOn) {
        ContractTerms terms = contracts.find(contractId).orElseThrow(ContractNotFoundForPaymentException::new);
        Set<Integer> paid = payments.paidInstallments(contractId);
        if (paid.contains(installmentNumber)) {
            throw new InstallmentAlreadyPaidException();
        }
        InstallmentTerms oldestUnpaid = terms.installments().stream()
                .filter(installment -> !paid.contains(installment.number()))
                .findFirst()
                .orElseThrow(InstallmentAlreadyPaidException::new);
        PaymentRules.oldestUnpaid(installmentNumber, oldestUnpaid.number());
        PaymentRules.withinRange(paidOn, terms.releaseDate(), LocalDate.now(clock.withZone(BRASILIA)));
        return oldestUnpaid;
    }
}
