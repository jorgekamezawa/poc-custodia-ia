package com.bancoexemplo.custody.contracts.application;

import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.BusinessCalendar;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.InstallmentSituation;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ContractQueryService {

    private static final int SEARCH_LIMIT = 50;
    private static final Pattern CPF_TYPED = Pattern.compile("[\\d.\\-\\s]+");
    private static final Pattern NOT_DIGIT = Pattern.compile("\\D");
    private static final int CPF_DIGITS = 11;

    private final ContractRepository repository;
    private final Clock clock;

    public ContractQueryService(ContractRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public ContractView view(UUID id) {
        return viewOf(repository.findById(id).orElseThrow(ContractNotFoundException::new));
    }

    public ContractView viewOf(Contract contract) {
        LocalDate today = BusinessCalendar.today(clock);
        return new ContractView(
                contract,
                contract.outstandingBalance(),
                contract.installments().stream()
                        .map(installment -> new ContractView.InstallmentView(
                                installment, InstallmentSituation.of(installment.dueDate(), today)))
                        .toList());
    }

    public List<ContractSummary> search(String query) {
        String text = query == null ? "" : query.strip();
        if (text.isEmpty()) {
            return repository.findLatest(SEARCH_LIMIT);
        }
        String digits = NOT_DIGIT.matcher(text).replaceAll("");
        if (CPF_TYPED.matcher(text).matches() && digits.length() == CPF_DIGITS) {
            return repository.findByCpf(digits, SEARCH_LIMIT);
        }
        return repository.findByNumberPrefix(text, SEARCH_LIMIT);
    }
}
