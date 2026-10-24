package com.bancoexemplo.custody.access.adapter.in;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableConfigurationProperties(ConfiguredUsers.class)
class SecurityConfig {

    private static final String INVALID_CREDENTIALS = "Usuário ou senha inválidos";

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, CurrentUsers currentUsers, ConfiguredUsers configuredUsers, JsonMapper json)
            throws Exception {
        int sessionSeconds = Math.toIntExact(configuredUsers.sessionTimeout().toSeconds());
        http.authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .csrf(csrf -> csrf.spa())
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .formLogin(form -> form.loginProcessingUrl("/api/auth/login")
                        .successHandler((request, response, authentication) -> {
                            request.getSession().setMaxInactiveInterval(sessionSeconds);
                            writeJson(response, HttpStatus.OK, currentUsers.of(authentication), json);
                        })
                        .failureHandler((request, response, exception) -> writeJson(
                                response, HttpStatus.UNAUTHORIZED, Map.of("message", INVALID_CREDENTIALS), json)))
                .logout(logout -> logout.logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(ConfiguredUsers configuredUsers) {
        return new InMemoryUserDetailsManager(configuredUsers.userDetails());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_SUPERVISOR > ROLE_OPERATOR");
    }

    @Bean
    CurrentUsers currentUsers(ConfiguredUsers configuredUsers, RoleHierarchy roleHierarchy) {
        return new CurrentUsers(configuredUsers.displayNames(), roleHierarchy);
    }

    private static void writeJson(HttpServletResponse response, HttpStatus status, Object body, JsonMapper json)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getWriter(), body);
    }
}
