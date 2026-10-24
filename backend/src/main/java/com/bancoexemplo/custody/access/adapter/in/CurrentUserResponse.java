package com.bancoexemplo.custody.access.adapter.in;

import java.util.List;

/** Who is logged in: the body of the login and of {@code GET /api/auth/me}. */
public record CurrentUserResponse(String username, String displayName, List<String> roles) {}
