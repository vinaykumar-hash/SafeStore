package com.vaultforge.service;

import com.vaultforge.exception.ConflictException;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ConcurrencyService {
  private final TransactionTemplate transactionTemplate;
  private final int maxRetries;
  private final long backoffMillis;

  public ConcurrencyService(PlatformTransactionManager transactionManager,
                            @Value("${vaultforge.concurrency.max-retries:3}") int maxRetries,
                            @Value("${vaultforge.concurrency.backoff-millis:25}") long backoffMillis) {
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.maxRetries = maxRetries;
    this.backoffMillis = backoffMillis;
  }

  public <T> T executeWithRetry(Supplier<T> supplier) {
    int attempts = 0;
    while (true) {
      try {
        return transactionTemplate.execute(status -> supplier.get());
      } catch (OptimisticLockingFailureException ex) {
        attempts++;
        if (attempts >= maxRetries) {
          throw new ConflictException("Concurrent update detected");
        }
        sleepBackoff();
      }
    }
  }

  public void runWithRetry(Runnable runnable) {
    executeWithRetry(() -> {
      runnable.run();
      return null;
    });
  }

  private void sleepBackoff() {
    if (backoffMillis <= 0) {
      return;
    }
    try {
      Thread.sleep(backoffMillis);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }
}
