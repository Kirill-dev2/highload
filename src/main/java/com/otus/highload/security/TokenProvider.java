package com.otus.highload.security;

import com.otus.highload.dao.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {
  private final Map<TokenBuildData, String> tokens = new ConcurrentHashMap<>();

  public String generateToken(User user, HttpServletRequest servletRequest) {
    log.info("Start create token for user.id [{}]", user.getId());
    var data = new TokenBuildData(user, servletRequest.getRemoteAddr());
    return tokens.computeIfAbsent(data, k -> buildToken(data));
  }

  private String buildToken(TokenBuildData data) {
    log.debug(
        "Start generation token for client name: [{}] with remote address: [{}]",
        data.user,
        data.remoteAddress);

    var now = Instant.now();

    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(data.user.getId())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
        .claim("user-id", data.user.getId())
        .claim("remote-address", data.remoteAddress)
        .signWith(Keys.hmacShaKeyFor(data.user.getPassword().getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  private record TokenBuildData(User user, String remoteAddress) {}
}
