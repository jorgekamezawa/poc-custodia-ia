package com.bancoexemplo.custody.payments.adapter.out.persistence;

import com.bancoexemplo.custody.payments.application.port.PaymentRepository;
import com.bancoexemplo.custody.payments.domain.InstallmentAlreadyPaidException;
import com.bancoexemplo.custody.payments.domain.Payment;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class JdbcPaymentRepository implements PaymentRepository {

    private final JdbcClient jdbc;

    JdbcPaymentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Set<Integer> paidInstallments(UUID contractId) {
        return new HashSet<>(jdbc.sql("SELECT installment_number FROM payment WHERE contract_id = :id")
                .param("id", contractId)
                .query(Integer.class)
                .list());
    }

    @Override
    public void save(Payment payment) {
        try {
            jdbc.sql("""
                            INSERT INTO payment (id, contract_id, installment_number, paid_on, amount, late_fee,
                                default_interest, registered_by, registered_at)
                            VALUES (:id, :contractId, :installmentNumber, :paidOn, :amount, :lateFee,
                                :defaultInterest, :registeredBy, :registeredAt)
                            """)
                    .param("id", payment.id())
                    .param("contractId", payment.contractId())
                    .param("installmentNumber", payment.installmentNumber())
                    .param("paidOn", payment.paidOn())
                    .param("amount", payment.amount())
                    .param("lateFee", payment.lateFee())
                    .param("defaultInterest", payment.defaultInterest())
                    .param("registeredBy", payment.registeredBy())
                    .param("registeredAt", Timestamp.from(payment.registeredAt()))
                    .update();
        } catch (DuplicateKeyException e) {
            throw new InstallmentAlreadyPaidException();
        }
    }
}
