package com.bancoexemplo.custody.contracts.adapter.in.web;

import com.bancoexemplo.custody.contracts.application.ContractQueryService;
import com.bancoexemplo.custody.contracts.application.RegisterContractCommand;
import com.bancoexemplo.custody.contracts.application.RegisterContractService;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.InvalidContractException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contracts")
class ContractController {

    private final RegisterContractService registerContract;
    private final ContractQueryService queries;

    ContractController(RegisterContractService registerContract, ContractQueryService queries) {
        this.registerContract = registerContract;
        this.queries = queries;
    }

    @GetMapping
    List<ContractSummaryResponse> search(@RequestParam(name = "q", required = false) String query) {
        return queries.search(query).stream().map(ContractSummaryResponse::of).toList();
    }

    @GetMapping("/{id}")
    ContractResponse view(@PathVariable UUID id) {
        return ContractResponse.of(queries.view(id));
    }

    @PostMapping
    ResponseEntity<ContractResponse> register(@RequestBody RegisterContractRequest request, Authentication user) {
        Contract contract = registerContract.register(new RegisterContractCommand(
                request.number(),
                request.customerName(),
                request.customerCpf(),
                request.chassisNumber(),
                request.vehicleDescription(),
                required(request.amortizationSystem(), "Sistema de amortização"),
                required(request.principal(), "Valor financiado"),
                required(request.monthlyRatePercent(), "Taxa ao mês"),
                required(request.termMonths(), "Prazo"),
                required(request.releaseDate(), "Data de liberação"),
                required(request.firstDueDate(), "Primeiro vencimento"),
                required(request.upfrontCosts(), "Custos pagos antecipadamente"),
                user.getName()));
        return ResponseEntity.created(URI.create("/api/contracts/" + contract.id()))
                .body(ContractResponse.of(queries.viewOf(contract)));
    }

    private static <T> T required(T value, String label) {
        if (value == null) {
            throw new InvalidContractException(label + " é obrigatório");
        }
        return value;
    }
}
