package com.bancoexemplo.custody.access.adapter.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

/** Through a real server, where an error status is forwarded to the error page, which MockMvc never does. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationServerTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("GER-03 0004/CT-8: sem o cabeçalho de CSRF o servidor responde 403, não 401")
    void missingCsrfHeaderAnswers403() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username=operador&password=operador-demo"))
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            assertThat(client.send(request, HttpResponse.BodyHandlers.discarding())
                            .statusCode())
                    .isEqualTo(403);
        }
    }

    @Test
    @DisplayName("GER-03: a página de erro chamada direto também exige login")
    void errorPageRequestedDirectlyNeedsLogin() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/error"))
                .GET()
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            assertThat(client.send(request, HttpResponse.BodyHandlers.discarding())
                            .statusCode())
                    .isEqualTo(401);
        }
    }
}
