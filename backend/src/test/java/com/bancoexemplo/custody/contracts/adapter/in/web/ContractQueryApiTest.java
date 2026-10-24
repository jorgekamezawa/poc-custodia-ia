package com.bancoexemplo.custody.contracts.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.contracts.ContractFixture;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.Contract;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ContractQueryApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ContractRepository repository;

    private MockHttpSession operatorSession() throws Exception {
        Cookie token =
                mvc.perform(get("/api/auth/me")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(token).isNotNull();
        return (MockHttpSession) mvc.perform(post("/api/auth/login")
                        .param("username", "operador")
                        .param("password", "operador-demo")
                        .cookie(token)
                        .header("X-XSRF-TOKEN", token.getValue()))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    private static String prefix() {
        return "Q" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private Contract saved(String number, String cpf, Instant registeredAt) {
        Contract contract = ContractFixture.contract(number, cpf, LocalDate.of(2027, 2, 10), registeredAt);
        repository.save(contract);
        return contract;
    }

    @Test
    @DisplayName("CON-06 0006/CT-6: a consulta traz o contrato, o saldo devedor e a situação das parcelas")
    void viewsContract() throws Exception {
        Contract contract = saved(prefix() + "-1", "12345678909", Instant.parse("2027-01-10T13:00:00Z"));

        mvc.perform(get("/api/contracts/" + contract.id()).session(operatorSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(contract.number().value()))
                .andExpect(jsonPath("$.outstandingBalance").value(10000.00))
                .andExpect(jsonPath("$.installments.length()").value(12))
                .andExpect(jsonPath("$.installments[0].situation").value("OPEN"));
    }

    @Test
    @DisplayName("0006/CT-7: contrato que não existe responde 404")
    void unknownContractAnswers404() throws Exception {
        mvc.perform(get("/api/contracts/" + UUID.randomUUID()).session(operatorSession()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Contrato não encontrado"));
    }

    @Test
    @DisplayName(
            "CON-05 0006/CT-8: busca pelo começo do número, sem diferenciar maiúscula, do mais novo ao mais antigo")
    void searchesByNumberPrefix() throws Exception {
        String prefix = prefix();
        saved(prefix + "-1", "12345678909", Instant.parse("2030-01-01T10:00:00Z"));
        saved(prefix + "-2", "12345678909", Instant.parse("2030-01-02T10:00:00Z"));
        saved(prefix() + "-3", "12345678909", Instant.parse("2030-01-03T10:00:00Z"));

        mvc.perform(get("/api/contracts")
                        .param("q", prefix.toLowerCase(Locale.ROOT))
                        .session(operatorSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].number").value(prefix + "-2"))
                .andExpect(jsonPath("$[1].number").value(prefix + "-1"))
                .andExpect(jsonPath("$[0].customerName").value("Cliente Exemplo"))
                .andExpect(jsonPath("$[0].principal").value(10000.00));
    }

    @Test
    @DisplayName("0006/CT-9: busca pelo CPF com pontuação")
    void searchesByCpf() throws Exception {
        Contract contract = saved(prefix() + "-1", "52998224725", Instant.parse("2027-01-10T13:00:00Z"));

        mvc.perform(get("/api/contracts").param("q", "529.982.247-25").session(operatorSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.number == '" + contract.number().value() + "')]")
                        .exists());
    }

    @Test
    @DisplayName("0006/CT-10: busca sem resultado responde lista vazia")
    void searchWithoutResult() throws Exception {
        mvc.perform(get("/api/contracts").param("q", "NAO-EXISTE").session(operatorSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("0006/CT-11: sem busca vêm os 50 mais recentes, do mais novo ao mais antigo")
    void latestFifty() throws Exception {
        String prefix = prefix();
        Instant base = Instant.parse("2099-01-01T00:00:00Z");
        for (int i = 1; i <= 51; i++) {
            saved(prefix + "-" + i, "12345678909", base.plusSeconds(i));
        }

        mvc.perform(get("/api/contracts").session(operatorSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(50))
                .andExpect(jsonPath("$[0].number").value(prefix + "-51"))
                .andExpect(jsonPath("$[49].number").value(prefix + "-2"));
    }

    @Test
    @DisplayName("GER-03 0006/CT-12: busca sem sessão responde 401")
    void searchWithoutSessionAnswers401() throws Exception {
        mvc.perform(get("/api/contracts")).andExpect(status().isUnauthorized());
    }

    @Test
    void percentAndUnderscoreAreSearchedLiterally() throws Exception {
        saved(prefix() + "-1", "12345678909", Instant.parse("2027-01-10T13:00:00Z"));

        mvc.perform(get("/api/contracts").param("q", "%").session(operatorSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/contracts").param("q", "_").session(operatorSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
