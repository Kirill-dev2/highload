package com.otus.highload.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthenticationUtil {

  public static String extractUserId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.isAuthenticated() && auth.getPrincipal() instanceof String userId) {
      return userId;
    } else {
      throw new AccessDeniedException("user mot Authenticated");
    }
  }
}
