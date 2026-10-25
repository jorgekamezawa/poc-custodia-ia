package com.bancoexemplo.custody.contracts.application;

import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.application.port.FinancingCalculator;
import com.bancoexemplo.custody.contracts.domain.contract.ChassisNumber;
import com.bancoexemplo.custody.contracts.domain.contract.Contract;
import com.bancoexemplo.custody.contracts.domain.contract.ContractNumber;
import com.bancoexemplo.custody.contracts.domain.contract.Cpf;
import com.bancoexemplo.custody.contracts.domain.contract.CustomerName;
import com.bancoexemplo.custody.contracts.domain.contract.DuplicateContractNumberException;
import com.bancoexemplo.custody.contracts.domain.contract.FinancingTerms;
import com.bancoexemplo.custody.contracts.domain.contract.VehicleDescription;
import java.time.Clock;
import java.time.Instant;
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
        CustomerName customerName = new CustomerName(command.customerName());
        Cpf cpf = new Cpf(command.customerCpf());
        ChassisNumber chassis = new ChassisNumber(command.chassisNumber());
        VehicleDescription vehicleDescription = new VehicleDescription(command.vehicleDescription());
        FinancingTerms terms = new FinancingTerms(
                command.amortizationSystem(),
                command.principal(),
                command.monthlyRatePercent(),
                command.termMonths(),
                command.releaseDate(),
                command.firstDueDate(),
                command.upfrontCosts());
        // Uniqueness spans contracts, so it is checked here against the repository, not inside one contract.
        if (repository.existsByNumber(number)) {
            throw new DuplicateContractNumberException();
        }
        Contract contract = Contract.create(
                number,
                customerName,
                cpf,
                chassis,
                vehicleDescription,
                terms,
                calculator.calculate(terms),
                command.registeredBy(),
                Instant.now(clock));
        repository.save(contract);
        return contract;
    }
}
