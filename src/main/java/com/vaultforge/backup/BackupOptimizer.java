package com.vaultforge.backup;

import com.vaultforge.dto.backup.BackupOptimizationMetrics;
import org.springframework.stereotype.Component;

@Component
public class BackupOptimizer {
  public BackupOptimizationMetrics buildMetrics(int totalChunks, int changedChunks) {
    int reused = Math.max(0, totalChunks - changedChunks);
    double ratio = totalChunks == 0 ? 0.0 : (double) changedChunks / totalChunks;
    return new BackupOptimizationMetrics(totalChunks, changedChunks, reused, ratio);
  }
}
