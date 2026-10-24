package com.bancoexemplo.custody.contracts.application;

import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.application.port.Financing;
import com.bancoexemplo.custody.contracts.application.port.FinancingCalculator;
import com.bancoexemplo.custody.contracts.application.port.FinancingRequest;
import com.bancoexemplo.custody.contracts.domain.ChassisNumber;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractNumber;
import com.bancoexemplo.custody.contracts.domain.ContractStatus;
import com.bancoexemplo.custody.contracts.domain.Cpf;
import com.bancoexemplo.custody.contracts.domain.DuplicateContractNumberException;
import com.bancoexemplo.custody.contracts.domain.TextField;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RegisterContractService {

    private final ContractRepository repository;
    private final FinancingCalculator calculator;
    private final Clock clock;

    public RegisterContractService(ContractRepository repository, FinancingCalculator calculator, Clock clock) {
        this.repository = repository;
        this.calculator = calculator;
        this.clock = clock;
    }

    public Contract register(RegisterContractCommand command) {
        ContractNumber number = new ContractNumber(command.number());
        String customerName = TextField.customerName(command.customerName());
        Cpf cpf = new Cpf(command.customerCpf());
        ChassisNumber chassis = new ChassisNumber(command.chassisNumber());
        String vehicleDescription = TextField.vehicleDescription(command.vehicleDescription());
        if (repository.existsByNumber(number)) {
            throw new DuplicateContractNumberException();
        }
        Financing financing = calculator.calculate(new FinancingRequest(
                command.amortizationSystem(),
                command.principal(),
                command.monthlyRatePercent(),
                command.termMonths(),
                command.releaseDate(),
                command.firstDueDate(),
                command.upfrontCosts()));
        Contract contract = new Contract(
                UUID.randomUUID(),
                number,
                customerName,
                cpf,
                chassis,
                vehicleDescription,
                command.amortizationSystem(),
                command.principal(),
                command.monthlyRatePercent(),
                command.termMonths(),
                command.releaseDate(),
                command.firstDueDate(),
                command.upfrontCosts(),
                financing.annualCetPercent(),
                ContractStatus.ACTIVE,
                command.registeredBy(),
                Instant.now(clock),
                financing.installments());
        repository.save(contract);
        return contract;
    }
}
