package com.bancoexemplo.custody.payments.adapter.out.persistence;

import com.bancoexemplo.custody.payments.application.port.PaymentRepository;
import com.bancoexemplo.custody.payments.domain.payment.Payment;
import com.bancoexemplo.custody.payments.domain.payment.exception.InstallmentAlreadyPaidException;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class PaymentRepositoryJpaImpl implements PaymentRepository {

    private static final String INSTALLMENT_KEY = "payment_contract_installment_key";

    private final EntityManager entityManager;
    private final PaymentJpaRepository payments;

    PaymentRepositoryJpaImpl(EntityManager entityManager, PaymentJpaRepository payments) {
        this.entityManager = entityManager;
        this.payments = payments;
    }

    @Override
    public Set<Integer> paidInstallments(UUID contractId) {
        return payments.findInstallmentNumbers(contractId);
    }

    @Override
    @Transactional
    public void save(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.id = payment.getId();
        entity.contractId = payment.getContractId();
        entity.installmentNumber = payment.getInstallmentNumber();
        entity.paidOn = payment.getPaidOn();
        entity.amount = payment.getAmount();
        entity.lateFee = payment.getLateFee();
        entity.defaultInterest = payment.getDefaultInterest();
        entity.registeredBy = payment.getRegisteredBy();
        entity.registeredAt = payment.getRegisteredAt();
        try {
            entityManager.persist(entity);
            // Flush here so a second payment of the installment surfaces in this method, as the domain's refusal.
            entityManager.flush();
        } catch (ConstraintViolationException e) {
            if (INSTALLMENT_KEY.equals(e.getConstraintName())) {
                throw new InstallmentAlreadyPaidException();
            }
            throw e;
        }
    }
}
