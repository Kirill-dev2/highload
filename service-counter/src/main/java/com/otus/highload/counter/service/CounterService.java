package com.otus.highload.counter.service;

import com.otus.highload.controller.dictionary.WorkflowStatuses;
import com.otus.highload.counter.dao.Counter;
import com.otus.highload.counter.repository.CounterRepositoryMaster;
import com.otus.highload.counter.repository.CounterRepositorySlave;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CounterService {
  private final CounterRepositoryMaster repositoryMaster;
  private final CounterRepositorySlave repositorySlave;

  public Long findAllUnreadByUserId(String userId) {
    return repositorySlave.findAllUnreadByUserId(userId);
  }

  public Long findAllUnreadByFromUserAndUserId(String toUser, String fromUser) {
    return repositorySlave.findAllUnreadByFromUserAndUserId(toUser, fromUser);
  }

  public void createNew(String id, String userId, String toUser) {
    repositoryMaster.save(new Counter(id, userId, toUser));
    log.debug("Save new counter.id [{}]", id);
  }

  public void updateStatus(String id, WorkflowStatuses status) {
    repositoryMaster.updateStatusById(id, status);
    log.debug("Update counter.id [{}] to status {}", id, status);
  }

  public void batchUpdateStatus(Set<String> ids, WorkflowStatuses status) {
    repositoryMaster.updateStatusByIds(ids, status);
    log.debug("Update message.ids {} to status {}", ids, status);
  }
}
