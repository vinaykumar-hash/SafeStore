package com.vaultforge.backup;

import com.vaultforge.dto.backup.BackupDiffResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

@Component
public class ChunkDiffEngine {
  public BackupDiffResult diffAgainstSnapshot(List<String> incomingHashes, String snapshotMetadata) {
    Map<Integer, String> snapshot = parse(snapshotMetadata);
    List<Integer> changedOrders = new ArrayList<>();
    for (int order = 0; order < incomingHashes.size(); order++) {
      String incoming = incomingHashes.get(order);
      String previous = snapshot.get(order);
      if (previous == null || !previous.equals(incoming)) {
        changedOrders.add(order);
      }
    }
    return new BackupDiffResult(changedOrders, incomingHashes.size(), changedOrders.size());
  }

  public BackupDiffResult diffSnapshots(String snapshotA, String snapshotB) {
    Map<Integer, String> left = parse(snapshotA);
    Map<Integer, String> right = parse(snapshotB);
    Set<Integer> orders = new TreeSet<>();
    orders.addAll(left.keySet());
    orders.addAll(right.keySet());
    List<Integer> changedOrders = new ArrayList<>();
    for (int order : orders) {
      String a = left.get(order);
      String b = right.get(order);
      if (a == null || b == null || !a.equals(b)) {
        changedOrders.add(order);
      }
    }
    return new BackupDiffResult(changedOrders, orders.size(), changedOrders.size());
  }

  private Map<Integer, String> parse(String snapshotMetadata) {
    Map<Integer, String> map = new HashMap<>();
    if (snapshotMetadata == null || snapshotMetadata.isBlank()) {
      return map;
    }
    String[] entries = snapshotMetadata.split(",");
    for (String entry : entries) {
      String[] parts = entry.split(":", 2);
      if (parts.length != 2) {
        continue;
      }
      try {
        int order = Integer.parseInt(parts[0]);
        map.put(order, parts[1]);
      } catch (NumberFormatException ex) {
        continue;
      }
    }
    return map;
  }
}
