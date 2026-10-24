package com.bancoexemplo.custody.payments.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class PaymentApiTest {

    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonMapper json;

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

    /** Registers the spec example released 44 days ago with the first installment due in 17 days. */
    private String registeredContract(MockHttpSession session) throws Exception {
        String body =
                """
                {"number": "CTR-P-%s", "customerName": "Cliente Exemplo", "customerCpf": "12345678909",
                 "chassisNumber": "9BWZZZ377VT004251", "vehicleDescription": "Hatch 1.0 2026",
                 "amortizationSystem": "PRICE", "principal": 10000.00, "monthlyRatePercent": 2,
                 "termMonths": 12, "releaseDate": "%s", "firstDueDate": "%s", "upfrontCosts": 0}
                """.formatted(UUID.randomUUID().toString().substring(0, 8), TODAY.minusDays(44), TODAY.plusDays(17));
        String response = mvc.perform(withCsrf(post("/api/contracts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .session(session))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return json.readTree(response).get("id").asString();
    }

    private MockHttpServletRequestBuilder pay(String contractId, int installment, String amount) throws Exception {
        return withCsrf(post("/api/contracts/" + contractId + "/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"installmentNumber": %d, "paidOn": "%s", "amount": %s}
                        """.formatted(installment, TODAY, amount)));
    }

    private JsonNode contract(String id, MockHttpSession session) throws Exception {
        return json.readTree(mvc.perform(get("/api/contracts/" + id).session(session))
                .andReturn()
                .getResponse()
                .getContentAsString());
    }

    @Test
    @DisplayName("PAG-01 0007/CT-16: pagamento registrado deixa a parcela paga e o saldo menor no contrato")
    void paymentMarksTheInstallmentPaid() throws Exception {
        MockHttpSession session = operatorSession();
        String contractId = registeredContract(session);

        mvc.perform(pay(contractId, 1, "945.60").session(session))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.installmentNumber").value(1))
                .andExpect(jsonPath("$.amount").value(945.60))
                .andExpect(jsonPath("$.registeredBy").value("operador"));

        await().atMost(Duration.ofSeconds(10))
                .until(() -> "PAID"
                        .equals(contract(contractId, session)
                                .get("installments")
                                .get(0)
                                .get("situation")
                                .asString()));
        assertThat(contract(contractId, session).get("outstandingBalance").decimalValue())
                .isEqualByComparingTo("9254.40");
    }

    @Test
    @DisplayName("PAG-04 0007/CT-17: segundo pagamento da mesma parcela responde 409")
    void secondPaymentAnswers409() throws Exception {
        MockHttpSession session = operatorSession();
        String contractId = registeredContract(session);
        mvc.perform(pay(contractId, 1, "945.60").session(session)).andExpect(status().isCreated());

        mvc.perform(pay(contractId, 1, "945.60").session(session))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Parcela já paga"));
    }

    @Test
    @DisplayName("PAG-02 0007/CT-18: valor diferente responde 400")
    void wrongAmountAnswers400() throws Exception {
        MockHttpSession session = operatorSession();
        String contractId = registeredContract(session);

        mvc.perform(pay(contractId, 1, "945.59").session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Valor deve ser igual ao da parcela: R$ 945,60"));
    }

    @Test
    @DisplayName("0007/CT-19: contrato que não existe responde 404")
    void unknownContractAnswers404() throws Exception {
        mvc.perform(pay(UUID.randomUUID().toString(), 1, "945.60").session(operatorSession()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Contrato não encontrado"));
    }

    @Test
    @DisplayName("GER-03 0007/CT-20: sem sessão o pagamento responde 401")
    void withoutSessionAnswers401() throws Exception {
        mvc.perform(pay(UUID.randomUUID().toString(), 1, "945.60")).andExpect(status().isUnauthorized());
    }

    @Test
    void missingFieldIsRefusedByName() throws Exception {
        MockHttpSession session = operatorSession();
        String contractId = registeredContract(session);

        mvc.perform(withCsrf(post("/api/contracts/" + contractId + "/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"installmentNumber\": 1}"))
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Data do pagamento é obrigatório"));
    }
}
