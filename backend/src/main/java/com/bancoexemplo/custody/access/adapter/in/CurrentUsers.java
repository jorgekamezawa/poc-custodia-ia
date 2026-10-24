package com.bancoexemplo.custody.access.adapter.in;

import java.util.Map;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/** Builds the response for the logged-in user, with every role the hierarchy grants. */
class CurrentUsers {

    private static final String ROLE_PREFIX = "ROLE_";

    private final Map<String, String> displayNames;
    private final RoleHierarchy roleHierarchy;

    CurrentUsers(Map<String, String> displayNames, RoleHierarchy roleHierarchy) {
        this.displayNames = displayNames;
        this.roleHierarchy = roleHierarchy;
    }

    CurrentUserResponse of(Authentication authentication) {
        var roles = roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities()).stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority != null && authority.startsWith(ROLE_PREFIX))
                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                .sorted()
                .toList();
        String username = authentication.getName();
        return new CurrentUserResponse(username, displayNames.getOrDefault(username, username), roles);
    }
}
