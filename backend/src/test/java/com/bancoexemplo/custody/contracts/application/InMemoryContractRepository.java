package com.bancoexemplo.custody.contracts.application;

import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractNumber;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class InMemoryContractRepository implements ContractRepository {

    final List<Contract> saved = new ArrayList<>();

    @Override
    public boolean existsByNumber(ContractNumber number) {
        return saved.stream().anyMatch(contract -> contract.number().equals(number));
    }

    @Override
    public void save(Contract contract) {
        saved.add(contract);
    }

    @Override
    public Optional<Contract> findById(UUID id) {
        return saved.stream().filter(contract -> contract.id().equals(id)).findFirst();
    }
}
