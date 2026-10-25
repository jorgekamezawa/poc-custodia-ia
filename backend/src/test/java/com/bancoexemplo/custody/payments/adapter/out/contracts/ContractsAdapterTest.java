package com.bancoexemplo.custody.payments.adapter.out.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.contracts.ContractFixture;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.payments.application.port.ContractToPay;
import com.bancoexemplo.custody.payments.application.port.ContractToPay.ContractTerms;
import com.bancoexemplo.custody.payments.application.port.ContractToPay.InstallmentTerms;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ContractsAdapterTest {

    @Autowired
    private ContractToPay contracts;

    @Autowired
    private ContractRepository contractRepository;

    @Test
    void registeredContractComesWithReleaseDateAndInstallmentTerms() {
        Contract contract = ContractFixture.contract(
                "CTR-TERMS-" + UUID.randomUUID().toString().substring(0, 8));
        contractRepository.save(contract);

        ContractTerms terms = contracts.find(contract.id()).orElseThrow();

        assertThat(terms.releaseDate()).isEqualTo(contract.releaseDate());
        assertThat(terms.installments()).hasSize(12);
        InstallmentTerms first = terms.installments().get(0);
        assertThat(first.number()).isEqualTo(1);
        assertThat(first.dueDate()).isEqualTo(LocalDate.of(2027, 2, 10));
        assertThat(first.amount()).isEqualByComparingTo("945.60");
        assertThat(terms.installments().get(11).amount()).isEqualByComparingTo("945.55");
    }

    @Test
    void unknownContractIsEmpty() {
        assertThat(contracts.find(UUID.randomUUID())).isEmpty();
    }
}
