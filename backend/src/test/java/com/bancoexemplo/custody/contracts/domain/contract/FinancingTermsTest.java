package com.bancoexemplo.custody.contracts.domain.contract;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.contracts.domain.contract.exception.InvalidContractException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FinancingTermsTest {

    private static final Object[] COMPLETE = {
        AmortizationSystem.PRICE,
        new BigDecimal("10000.00"),
        new BigDecimal("2"),
        12,
        LocalDate.of(2027, 1, 10),
        LocalDate.of(2027, 2, 10),
        BigDecimal.ZERO
    };

    static Stream<Arguments> eachTermMissing() {
        String[] labels = {
            "Sistema de amortização",
            "Valor financiado",
            "Taxa ao mês",
            "Prazo",
            "Data de liberação",
            "Primeiro vencimento",
            "Custos pagos antecipadamente"
        };
        return IntStream.range(0, labels.length).mapToObj(i -> Arguments.of(i, labels[i]));
    }

    @ParameterizedTest(name = "{displayName} [{arguments}]")
    @MethodSource("eachTermMissing")
    void missingTermIsRefusedByName(int missing, String label) {
        Object[] terms = COMPLETE.clone();
        terms[missing] = null;

        assertThatThrownBy(() -> new FinancingTerms(
                        (AmortizationSystem) terms[0],
                        (BigDecimal) terms[1],
                        (BigDecimal) terms[2],
                        (Integer) terms[3],
                        (LocalDate) terms[4],
                        (LocalDate) terms[5],
                        (BigDecimal) terms[6]))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage(label + " é obrigatório");
    }
}
