package com.bancoexemplo.custody.contracts.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContractFieldsTest {

    private static final String CHASSIS_MESSAGE = "Chassi deve ter 17 letras e números, sem I, O e Q";
    private static final String NUMBER_MESSAGE = "Número do contrato deve ter de 1 a 30 letras, números ou hífen";
    private static final String NAME_MESSAGE = "Nome do cliente deve ter de 1 a 120 caracteres";

    @Test
    void validCpfIsAccepted() {
        assertThat(new Cpf("12345678909").digits()).isEqualTo("12345678909");
    }

    @Test
    @DisplayName("CON-02 0005/CT-3: CPF com dígito verificador errado é recusado")
    void cpfWithWrongCheckDigit() {
        assertThatThrownBy(() -> new Cpf("12345678900"))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage("CPF inválido");
    }

    @Test
    @DisplayName("CON-02 0005/CT-4: CPF com todos os dígitos iguais é recusado")
    void cpfWithRepeatedDigits() {
        assertThatThrownBy(() -> new Cpf("11111111111"))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage("CPF inválido");
    }

    @Test
    void cpfWithLettersOrWrongLengthIsRefused() {
        assertThatThrownBy(() -> new Cpf("1234567890")).hasMessage("CPF inválido");
        assertThatThrownBy(() -> new Cpf("1234567890a")).hasMessage("CPF inválido");
        assertThatThrownBy(() -> new Cpf(null)).hasMessage("CPF inválido");
    }

    @Test
    void cpfWhoseFirstCheckDigitIsWrongIsRefused() {
        assertThatThrownBy(() -> new Cpf("12345678919")).hasMessage("CPF inválido");
    }

    @Test
    void cpfWithCheckDigitZeroIsAccepted() {
        assertThat(new Cpf("52998224725").digits()).isEqualTo("52998224725");
        assertThat(new Cpf("11144477735").digits()).isEqualTo("11144477735");
    }

    @Test
    void validChassisIsAccepted() {
        assertThat(new ChassisNumber("9BWZZZ377VT004251").value()).isEqualTo("9BWZZZ377VT004251");
    }

    @Test
    @DisplayName("CON-02 0005/CT-5: chassi com 16 caracteres é recusado")
    void chassisWithSixteenCharacters() {
        assertThatThrownBy(() -> new ChassisNumber("9BWZZZ377VT00425"))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage(CHASSIS_MESSAGE);
    }

    @Test
    @DisplayName("CON-02 0005/CT-6: chassi com a letra I é recusado")
    void chassisWithLetterI() {
        assertThatThrownBy(() -> new ChassisNumber("9BWZZZ377VT00425I"))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage(CHASSIS_MESSAGE);
    }

    @Test
    void chassisWithLettersOAndQIsRefused() {
        assertThatThrownBy(() -> new ChassisNumber("9BWZZZ377VT00425O")).hasMessage(CHASSIS_MESSAGE);
        assertThatThrownBy(() -> new ChassisNumber("9BWZZZ377VT00425Q")).hasMessage(CHASSIS_MESSAGE);
        assertThatThrownBy(() -> new ChassisNumber(null)).hasMessage(CHASSIS_MESSAGE);
    }

    @Test
    void validContractNumberIsAccepted() {
        assertThat(new ContractNumber("CTR-0001").value()).isEqualTo("CTR-0001");
        assertThat(new ContractNumber("A".repeat(30)).value()).hasSize(30);
    }

    @Test
    @DisplayName("CON-02 0005/CT-7: número do contrato com 31 caracteres é recusado")
    void contractNumberTooLong() {
        assertThatThrownBy(() -> new ContractNumber("A".repeat(31)))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage(NUMBER_MESSAGE);
    }

    @Test
    @DisplayName("CON-02 0005/CT-8: número do contrato com espaço é recusado")
    void contractNumberWithSpace() {
        assertThatThrownBy(() -> new ContractNumber("CTR 0001"))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage(NUMBER_MESSAGE);
    }

    @Test
    void emptyContractNumberIsRefused() {
        assertThatThrownBy(() -> new ContractNumber("")).hasMessage(NUMBER_MESSAGE);
        assertThatThrownBy(() -> new ContractNumber(null)).hasMessage(NUMBER_MESSAGE);
    }

    @Test
    @DisplayName("CON-02 0005/CT-9: nome do cliente vazio é recusado")
    void emptyCustomerName() {
        assertThatThrownBy(() -> TextField.customerName("  "))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage(NAME_MESSAGE);
    }

    @Test
    @DisplayName("CON-02 0005/CT-10: nome do cliente com 121 caracteres é recusado")
    void customerNameTooLong() {
        assertThatThrownBy(() -> TextField.customerName("A".repeat(121)))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage(NAME_MESSAGE);
    }

    @Test
    void customerNameIsTrimmedAndAcceptedUpTo120() {
        assertThat(TextField.customerName("  Cliente Exemplo  ")).isEqualTo("Cliente Exemplo");
        assertThat(TextField.customerName("A".repeat(120))).hasSize(120);
        assertThatThrownBy(() -> TextField.customerName(null)).hasMessage(NAME_MESSAGE);
    }

    @Test
    @DisplayName("CON-02 0005/CT-11: descrição do veículo vazia é recusada")
    void emptyVehicleDescription() {
        assertThatThrownBy(() -> TextField.vehicleDescription(""))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage("Descrição do veículo deve ter de 1 a 120 caracteres");
    }
}
