package com.bancoexemplo.custody.access.adapter.in;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Users and session length read from configuration, so the portal needs no identity server.
 *
 * @param users each with a password hash in the {@code {bcrypt}...} form
 */
@ConfigurationProperties(prefix = "custody.security")
public record ConfiguredUsers(List<ConfiguredUser> users, Duration sessionTimeout) {

    public ConfiguredUsers {
        users = List.copyOf(users);
    }

    /** A portal user; {@code role} is {@code OPERATOR} or {@code SUPERVISOR}. */
    public record ConfiguredUser(String username, String password, String displayName, String role) {}

    List<UserDetails> userDetails() {
        return users.stream()
                .map(user -> User.withUsername(user.username())
                        .password(user.password())
                        .roles(user.role())
                        .build())
                .toList();
    }

    Map<String, String> displayNames() {
        return users.stream()
                .collect(Collectors.toUnmodifiableMap(ConfiguredUser::username, ConfiguredUser::displayName));
    }
}
