package com.bancoexemplo.custody.payments.domain.payment;

import com.bancoexemplo.custody.payments.domain.payment.exception.InstallmentAlreadyPaidException;
import com.bancoexemplo.custody.payments.domain.payment.exception.InvalidPaymentException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** The contract as a payment is checked against: release date and installments, in order. */
public record ContractTerms(LocalDate releaseDate, List<InstallmentTerms> installments) {

    public ContractTerms {
        installments = List.copyOf(installments);
    }

    /**
     * The installment that may be paid on the date: the oldest unpaid one, paid between the release and today.
     *
     * @param paid numbers of the installments already paid
     */
    public InstallmentTerms payable(Integer installmentNumber, LocalDate paidOn, Set<Integer> paid, LocalDate today) {
        if (installmentNumber == null) {
            throw new InvalidPaymentException("Parcela é obrigatório");
        }
        if (paidOn == null) {
            throw new InvalidPaymentException("Data do pagamento é obrigatório");
        }
        if (paid.contains(installmentNumber)) {
            throw new InstallmentAlreadyPaidException();
        }
        InstallmentTerms oldestUnpaid = installments.stream()
                .filter(installment -> !paid.contains(installment.number()))
                .findFirst()
                .orElseThrow(InstallmentAlreadyPaidException::new);
        if (installmentNumber != oldestUnpaid.number()) {
            throw new InvalidPaymentException("Pague antes a parcela " + oldestUnpaid.number());
        }
        if (paidOn.isBefore(releaseDate) || paidOn.isAfter(today)) {
            throw new InvalidPaymentException("Data do pagamento deve ficar entre a liberação e hoje");
        }
        return oldestUnpaid;
    }
}
