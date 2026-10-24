package com.bancoexemplo.custody.payments.application;

import com.bancoexemplo.custody.payments.application.port.ContractToPay;
import com.bancoexemplo.custody.payments.application.port.PaymentEvents;
import com.bancoexemplo.custody.payments.application.port.PaymentRepository;
import com.bancoexemplo.custody.payments.domain.InstallmentAlreadyPaidException;
import com.bancoexemplo.custody.payments.domain.Payment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** In-memory stand-ins for the payment ports. */
final class PaymentFakes {

    private PaymentFakes() {}

    static final class Payments implements PaymentRepository {
        final List<Payment> saved = new ArrayList<>();

        @Override
        public Set<Integer> paidInstallments(UUID contractId) {
            return saved.stream()
                    .filter(payment -> payment.contractId().equals(contractId))
                    .map(Payment::installmentNumber)
                    .collect(Collectors.toSet());
        }

        @Override
        public void save(Payment payment) {
            if (paidInstallments(payment.contractId()).contains(payment.installmentNumber())) {
                throw new InstallmentAlreadyPaidException();
            }
            saved.add(payment);
        }
    }

    /** One contract released on 10/09/2026 with 12 installments of R$ 945.60 from 10/11/2026. */
    static final class Contracts implements ContractToPay {
        static final UUID ID = UUID.fromString("00000000-0000-4000-8000-000000000001");
        static final LocalDate RELEASE = LocalDate.of(2026, 9, 10);
        static final LocalDate FIRST_DUE = LocalDate.of(2026, 11, 10);

        /** A second contract, released on 10/09/2026, whose first installment was due on 10/10/2026. */
        static final UUID LATE_ID = UUID.fromString("00000000-0000-4000-8000-000000000002");

        @Override
        public Optional<ContractTerms> find(UUID contractId) {
            if (ID.equals(contractId)) {
                return Optional.of(terms(FIRST_DUE));
            }
            if (LATE_ID.equals(contractId)) {
                return Optional.of(terms(LocalDate.of(2026, 10, 10)));
            }
            return Optional.empty();
        }

        private static ContractTerms terms(LocalDate firstDue) {
            List<InstallmentTerms> installments = new ArrayList<>();
            for (int number = 1; number <= 12; number++) {
                installments.add(
                        new InstallmentTerms(number, firstDue.plusMonths(number - 1L), new BigDecimal("945.60")));
            }
            return new ContractTerms(RELEASE, installments);
        }
    }

    static final class Events implements PaymentEvents {
        final List<Payment> published = new ArrayList<>();

        @Override
        public void paymentRegistered(Payment payment) {
            published.add(payment);
        }
    }
}
