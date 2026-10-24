package com.bancoexemplo.custody.payments.domain;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;

/** What makes a payment acceptable. */
public final class PaymentRules {

    private PaymentRules() {}

    /** Only the oldest unpaid installment may be paid. */
    public static void oldestUnpaid(int requested, int oldestUnpaid) {
        if (requested != oldestUnpaid) {
            throw new InvalidPaymentException("Pague antes a parcela " + oldestUnpaid);
        }
    }

    /** The amount paid is exactly what is due on the payment date. */
    public static void exactAmount(BigDecimal paid, LateCharges due) {
        if (paid.compareTo(due.total()) != 0) {
            throw new InvalidPaymentException((due.late()
                            ? "Valor deve ser igual ao da parcela com multa e juros de mora: R$ "
                            : "Valor deve ser igual ao da parcela: R$ ")
                    + reais(due.total()));
        }
    }

    /** Paid between the release and today. */
    public static void withinRange(LocalDate paidOn, LocalDate releaseDate, LocalDate today) {
        if (paidOn.isBefore(releaseDate) || paidOn.isAfter(today)) {
            throw new InvalidPaymentException("Data do pagamento deve ficar entre a liberação e hoje");
        }
    }

    private static String reais(BigDecimal amount) {
        return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.of("pt", "BR"))).format(amount);
    }
}
