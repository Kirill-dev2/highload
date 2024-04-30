package com.otus.highload.service.tarantool;

import com.otus.highload.controller.response.DialogMessage;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tarantool.TarantoolClient;

@Service
@RequiredArgsConstructor
public class TarantoolService {
  private final TarantoolClient tarantoolClient;

  public List<DialogMessage> listMessages(String userId, String toUser) {
    var result = tarantoolClient.syncOps().call("search", userId.hashCode() + toUser.hashCode());
    if (result.isEmpty()) {
      return Collections.emptyList();
    }
    var messages = (List<?>) result.getFirst();
    var stream = messages.size() > 500 ? messages.parallelStream() : messages.stream();

    return stream
        .map(q -> (List<?>) q)
        .map(p -> p.get(2))
        .map(q -> (List<?>) q)
        .map(
            payload ->
                new DialogMessage(
                    payload.getFirst().toString(),
                    payload.get(1).toString(),
                    payload.get(2).toString()))
        .toList();
  }

  public void save(String userId, String toUser, String text) {
    tarantoolClient
        .syncOps()
        .call(
            "save",
            UUID.randomUUID().toString(),
            userId.hashCode() + toUser.hashCode(),
            List.of(userId, toUser, text));
  }
}
