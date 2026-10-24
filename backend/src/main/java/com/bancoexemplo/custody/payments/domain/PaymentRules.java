package com.bancoexemplo.custody.payments.domain;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;

/** What makes an on-time payment acceptable. */
public final class PaymentRules {

    private PaymentRules() {}

    /** Only the oldest unpaid installment may be paid. */
    public static void oldestUnpaid(int requested, int oldestUnpaid) {
        if (requested != oldestUnpaid) {
            throw new InvalidPaymentException("Pague antes a parcela " + oldestUnpaid);
        }
    }

    /** The amount paid is exactly the installment amount. */
    public static void exactAmount(BigDecimal paid, BigDecimal due) {
        if (paid.compareTo(due) != 0) {
            throw new InvalidPaymentException("Valor deve ser igual ao da parcela: R$ " + reais(due));
        }
    }

    /** Paid between the release and today, and not after the due date. */
    public static void onTime(LocalDate paidOn, LocalDate releaseDate, LocalDate today, LocalDate dueDate) {
        if (paidOn.isBefore(releaseDate) || paidOn.isAfter(today)) {
            throw new InvalidPaymentException("Data do pagamento deve ficar entre a liberação e hoje");
        }
        if (paidOn.isAfter(dueDate)) {
            throw new InvalidPaymentException("Pagamento depois do vencimento ainda não pode ser registrado");
        }
    }

    private static String reais(BigDecimal amount) {
        return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.of("pt", "BR"))).format(amount);
    }
}
