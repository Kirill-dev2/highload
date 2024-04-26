package com.otus.highload.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq")
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  private String host;
  private Integer port;
  private String username;
  private String password;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry
        .enableStompBrokerRelay("/queue")
        .setRelayPort(port)
        .setRelayHost(host)
        .setClientPasscode(password)
        .setClientLogin(username);
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/post/feed/posted").setAllowedOriginPatterns("*");
    registry.addEndpoint("/post/feed/posted").setAllowedOriginPatterns("*").withSockJS();
  }
}
