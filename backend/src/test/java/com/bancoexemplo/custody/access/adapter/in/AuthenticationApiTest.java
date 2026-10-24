package com.bancoexemplo.custody.access.adapter.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationApiTest {

    @Autowired
    private MockMvc mvc;

    /** Does what the portal does: reads the CSRF cookie from a first response and sends it back as a header. */
    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) throws Exception {
        Cookie token =
                mvc.perform(get("/api/auth/me")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(token).isNotNull();
        return request.cookie(token).header("X-XSRF-TOKEN", token.getValue());
    }

    private MockHttpServletRequestBuilder login(String username, String password) throws Exception {
        return withCsrf(post("/api/auth/login").param("username", username).param("password", password));
    }

    private MockHttpSession loggedInAs(String username, String password) throws Exception {
        MvcResult result = mvc.perform(login(username, password))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("GER-03 0004/CT-1: operador entra com a senha certa")
    void operatorLogsIn() throws Exception {
        mvc.perform(login("operador", "operador-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("operador"))
                .andExpect(jsonPath("$.displayName").value("Operador"))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("OPERATOR"));
    }

    @Test
    @DisplayName("GER-03 0004/CT-2: senha errada é recusada sem dizer o que errou")
    void wrongPasswordIsRefused() throws Exception {
        mvc.perform(login("operador", "senha-errada"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos"));
    }

    @Test
    @DisplayName("GER-03 0004/CT-3: usuário inexistente recebe a mesma recusa")
    void unknownUserIsRefusedTheSameWay() throws Exception {
        mvc.perform(login("usuario-inexistente", "qualquer"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos"));
    }

    @Test
    @DisplayName("GER-03 0004/CT-4: sem sessão a API responde 401 e entrega o cookie de CSRF")
    void withoutSessionTheApiAnswers401() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized()).andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    @DisplayName("GER-03 0004/CT-5: depois de sair a sessão deixa de valer")
    void logoutEndsTheSession() throws Exception {
        MockHttpSession session = loggedInAs("operador", "operador-demo");
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk());

        mvc.perform(withCsrf(post("/api/auth/logout")).session(session)).andExpect(status().isNoContent());

        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GER-04 0004/CT-6: supervisor tem também o perfil de operador")
    void supervisorAlsoHasTheOperatorRole() throws Exception {
        MockHttpSession session = loggedInAs("supervisor", "supervisor-demo");

        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Supervisor"))
                .andExpect(jsonPath("$.roles[0]").value("OPERATOR"))
                .andExpect(jsonPath("$.roles[1]").value("SUPERVISOR"));
    }

    @Test
    @DisplayName("GER-05 0004/CT-7: a sessão expira depois de 30 minutos sem uso")
    void sessionExpiresAfterThirtyMinutes() throws Exception {
        assertThat(loggedInAs("operador", "operador-demo").getMaxInactiveInterval())
                .isEqualTo(1800);
    }

    @Test
    @DisplayName("GER-03 0004/CT-8: pedido que altera dado sem o cabeçalho de CSRF é recusado")
    void requestWithoutCsrfHeaderIsRefused() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "operador").param("password", "operador-demo"))
                .andExpect(status().isForbidden());
    }
}
