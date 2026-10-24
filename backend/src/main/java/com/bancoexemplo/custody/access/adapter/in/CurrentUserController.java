package com.bancoexemplo.custody.access.adapter.in;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class CurrentUserController {

    private final CurrentUsers currentUsers;

    CurrentUserController(CurrentUsers currentUsers) {
        this.currentUsers = currentUsers;
    }

    @GetMapping("/me")
    CurrentUserResponse me(Authentication authentication) {
        return currentUsers.of(authentication);
    }
}
