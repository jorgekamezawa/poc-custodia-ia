package com.bancoexemplo.custody.contracts.domain.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.contracts.ContractFixture;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContractQueryRulesTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 24);

    @Test
    @DisplayName("CON-05 0006/CT-1: parcela que vence hoje está em aberto")
    void dueTodayIsOpen() {
        assertThat(InstallmentSituation.of(TODAY, TODAY)).isEqualTo(InstallmentSituation.OPEN);
    }

    @Test
    @DisplayName("CON-05 0006/CT-2: parcela que venceu ontem está vencida")
    void dueYesterdayIsOverdue() {
        assertThat(InstallmentSituation.of(TODAY.minusDays(1), TODAY)).isEqualTo(InstallmentSituation.OVERDUE);
    }

    @Test
    @DisplayName("CON-05 0006/CT-3: parcela que vence no futuro está em aberto")
    void dueInTheFutureIsOpen() {
        assertThat(InstallmentSituation.of(LocalDate.of(2026, 11, 10), TODAY)).isEqualTo(InstallmentSituation.OPEN);
    }

    @Test
    @DisplayName("CON-05 0006/CT-4: hoje é a data de Brasília, não a do UTC")
    void todayIsTheBrasiliaDate() {
        Clock lateNightInBrasilia = Clock.fixed(Instant.parse("2026-10-25T02:00:00Z"), ZoneOffset.UTC);

        assertThat(BusinessCalendar.today(lateNightInBrasilia)).isEqualTo(TODAY);
        assertThat(InstallmentSituation.of(TODAY, BusinessCalendar.today(lateNightInBrasilia)))
                .isEqualTo(InstallmentSituation.OPEN);
    }

    @Test
    @DisplayName("CON-06 0006/CT-5: sem parcela paga o saldo devedor é o valor financiado")
    void outstandingBalanceWithoutPaymentsIsThePrincipal() {
        assertThat(ContractFixture.contract("CTR-0001").outstandingBalance()).isEqualByComparingTo("10000.00");
    }
}
