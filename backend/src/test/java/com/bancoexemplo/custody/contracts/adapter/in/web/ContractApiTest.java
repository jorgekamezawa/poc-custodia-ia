package com.bancoexemplo.custody.contracts.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ContractApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcClient jdbc;

    private static String uniqueNumber() {
        return "CTR-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String body(String number, String cpf, int termMonths, String releaseDate) {
        return """
                {"number": "%s", "customerName": "Cliente Exemplo", "customerCpf": "%s",
                 "chassisNumber": "9BWZZZ377VT004251", "vehicleDescription": "Hatch 1.0 2026",
                 "amortizationSystem": "PRICE", "principal": 10000.00, "monthlyRatePercent": 2,
                 "termMonths": %d, "releaseDate": "%s", "firstDueDate": "2027-02-10", "upfrontCosts": 0}
                """.formatted(number, cpf, termMonths, releaseDate);
    }

    private static String validBody(String number) {
        return body(number, "12345678909", 12, "2027-01-10");
    }

    /** Does what the portal does: reads the CSRF cookie from a first response and sends it back as a header. */
    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) throws Exception {
        Cookie token =
                mvc.perform(get("/api/auth/me")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(token).isNotNull();
        return request.cookie(token).header("X-XSRF-TOKEN", token.getValue());
    }

    private MockHttpSession operatorSession() throws Exception {
        return (MockHttpSession) mvc.perform(withCsrf(
                        post("/api/auth/login").param("username", "operador").param("password", "operador-demo")))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    private MockHttpServletRequestBuilder register(String json) throws Exception {
        return withCsrf(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .session(operatorSession());
    }

    private long stored(String number) {
        return jdbc.sql("SELECT count(*) FROM contract WHERE number = :number")
                .param("number", number)
                .query(Long.class)
                .single();
    }

    @Test
    @DisplayName("CON-03 0005/CT-15: cadastro aceito devolve o contrato ativo com cronograma e CET")
    void registersContract() throws Exception {
        String number = uniqueNumber();

        mvc.perform(register(validBody(number)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/contracts/")))
                .andExpect(jsonPath("$.number").value(number))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.annualCetPercent").value(26.95))
                .andExpect(jsonPath("$.installments.length()").value(12))
                .andExpect(jsonPath("$.installments[0].amount").value(945.60))
                .andExpect(jsonPath("$.installments[11].amount").value(945.55));
        assertThat(stored(number)).isEqualTo(1);
    }

    @Test
    @DisplayName("CON-01 0005/CT-16: número repetido responde 409")
    void duplicateNumberAnswers409() throws Exception {
        String number = uniqueNumber();
        mvc.perform(register(validBody(number))).andExpect(status().isCreated());

        mvc.perform(register(validBody(number)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Número do contrato já cadastrado"));
        assertThat(stored(number)).isEqualTo(1);
    }

    @Test
    @DisplayName("CON-02 0005/CT-17: CPF inválido responde 400 e nada é guardado")
    void invalidCpfAnswers400() throws Exception {
        String number = uniqueNumber();

        mvc.perform(register(body(number, "12345678900", 12, "2027-01-10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("CPF inválido"));
        assertThat(stored(number)).isZero();
    }

    @Test
    @DisplayName("CAL-03 0005/CT-18: prazo fora do limite responde 400 com a mensagem de Calculadoras")
    void termOutOfLimitsAnswers400() throws Exception {
        String number = uniqueNumber();

        mvc.perform(register(body(number, "12345678909", 73, "2027-01-10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Prazo deve estar entre 1 e 72 meses"));
        assertThat(stored(number)).isZero();
    }

    @Test
    @DisplayName("CAL-06 0005/CT-19: liberação no dia do primeiro vencimento responde 400")
    void releaseOnFirstDueDateAnswers400() throws Exception {
        mvc.perform(register(body(uniqueNumber(), "12345678909", 12, "2027-02-10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Data de liberação deve ser anterior ao primeiro vencimento"));
    }

    @Test
    @DisplayName("CON-04 0005/CT-20: o contrato guarda o usuário que cadastrou")
    void recordsTheUser() throws Exception {
        mvc.perform(register(validBody(uniqueNumber())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registeredBy").value("operador"))
                .andExpect(jsonPath("$.registeredAt").exists());
    }

    @Test
    @DisplayName("GER-03 0005/CT-21: sem sessão o cadastro responde 401")
    void withoutSessionAnswers401() throws Exception {
        mvc.perform(withCsrf(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(uniqueNumber()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingFieldIsRefusedByName() throws Exception {
        mvc.perform(register("{\"number\": \"" + uniqueNumber() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Sistema de amortização é obrigatório"));
    }
}
