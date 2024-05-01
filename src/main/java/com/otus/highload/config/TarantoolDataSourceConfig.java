package com.otus.highload.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tarantool.TarantoolClient;
import org.tarantool.TarantoolClientConfig;
import org.tarantool.TarantoolClientImpl;
import ru.shadam.tarantool.core.SimpleSocketChannelProvider;

@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.datasource-tarantool")
public class TarantoolDataSourceConfig {
  private String host;
  private int port;

  @Bean(destroyMethod = "close")
  public TarantoolClient tarantoolClient() {
    var channelProvider = new SimpleSocketChannelProvider(host, port);
    return new TarantoolClientImpl(channelProvider, new TarantoolClientConfig());
  }
}
