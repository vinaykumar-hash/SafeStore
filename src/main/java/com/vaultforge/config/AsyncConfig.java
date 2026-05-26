package com.vaultforge.config;

import java.util.concurrent.Executor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskDecorator;

@Configuration
public class AsyncConfig {
  @Bean(name = "vaultforgeExecutor")
  public Executor vaultforgeExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(8);
    executor.setMaxPoolSize(32);
    executor.setQueueCapacity(2000);
    executor.setThreadNamePrefix("vf-exec-");
    executor.setAwaitTerminationSeconds(30);
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setTaskDecorator(mdcTaskDecorator());
    executor.initialize();
    return executor;
  }

  @Bean
  public TaskDecorator mdcTaskDecorator() {
    return runnable -> {
      var contextMap = MDC.getCopyOfContextMap();
      return () -> {
        if (contextMap != null) {
          MDC.setContextMap(contextMap);
        }
        try {
          runnable.run();
        } finally {
          MDC.clear();
        }
      };
    };
  }
}
