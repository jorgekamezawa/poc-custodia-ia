package com.bancoexemplo.custody.contracts.application;

import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractNumber;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

class InMemoryContractRepository implements ContractRepository {

    final List<Contract> saved = new ArrayList<>();
    String lastSearch;

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

    @Override
    public List<ContractSummary> findLatest(int limit) {
        lastSearch = "latest";
        return summaries(saved, limit);
    }

    @Override
    public List<ContractSummary> findByCpf(String digits, int limit) {
        lastSearch = "cpf:" + digits;
        return summaries(
                saved.stream()
                        .filter(c -> c.customerCpf().digits().equals(digits))
                        .toList(),
                limit);
    }

    @Override
    public List<ContractSummary> findByNumberPrefix(String prefix, int limit) {
        lastSearch = "number:" + prefix;
        return summaries(
                saved.stream()
                        .filter(c ->
                                c.number().value().toUpperCase(Locale.ROOT).startsWith(prefix.toUpperCase(Locale.ROOT)))
                        .toList(),
                limit);
    }

    private static List<ContractSummary> summaries(List<Contract> contracts, int limit) {
        return contracts.stream()
                .sorted(Comparator.comparing(Contract::registeredAt).reversed())
                .limit(limit)
                .map(ContractSummary::of)
                .toList();
    }

    @Override
    public void markInstallmentPaid(UUID contractId, int installmentNumber, LocalDate paidOn) {
        for (int i = 0; i < saved.size(); i++) {
            if (saved.get(i).id().equals(contractId)) {
                saved.set(i, saved.get(i).withInstallmentPaid(installmentNumber, paidOn));
            }
        }
    }
}
