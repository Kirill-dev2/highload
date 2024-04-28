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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {
  public static final String HEADER_NAME = "Authorization";
  public static final String USER_ID = "user-id";

  @Value("${token.signing.public-key}")
  private String publicKey;

  private final Map<TokenBuildData, String> tokens = new ConcurrentHashMap<>();

  public String generateToken(User user, HttpServletRequest request) {
    if (!publicKey.equals(request.getHeader(HEADER_NAME))) {
      throw new AccessDeniedException("different public-key");
    }
    log.info("token for user.id [{}]", user.getId());
    var data = new TokenBuildData(user, request.getRemoteAddr());
    return tokens.computeIfAbsent(data, k -> buildToken(data));
  }

  public Object determinateUserId(HttpServletRequest request) {
    var headerToken = request.getHeader(HEADER_NAME);
    var parameterToken = request.getParameter(HEADER_NAME);
    if (StringUtils.isEmpty(headerToken) && StringUtils.isEmpty(parameterToken)) {
      throw new AccessDeniedException("absent token");
    }
    var token = StringUtils.isNotEmpty(headerToken) ? headerToken : parameterToken;

    return Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(publicKey.getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get(USER_ID);
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
        .claim(USER_ID, data.user.getId())
        .claim("remote-address", data.remoteAddress)
        .signWith(Keys.hmacShaKeyFor(publicKey.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  private record TokenBuildData(User user, String remoteAddress) {}
}
