package com.vaultforge.metadata;

import com.vaultforge.domain.FileMetadata;
import com.vaultforge.dto.metadata.MetadataSearchRequest;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class MetadataSpecifications {
  private MetadataSpecifications() {}

  public static Specification<FileMetadata> from(MetadataSearchRequest request) {
    return Specification.where(filenameLike(request.filename()))
        .and(ownerEquals(request.ownerId()))
        .and(tagsLike(request.tags()))
        .and(uploadedAfter(request.uploadedAfter()))
        .and(uploadedBefore(request.uploadedBefore()));
  }

  private static Specification<FileMetadata> filenameLike(String filename) {
    if (filename == null || filename.isBlank()) {
      return null;
    }
    return (root, query, builder) -> builder.like(builder.lower(root.get("filename")),
        "%" + filename.toLowerCase() + "%");
  }

  private static Specification<FileMetadata> ownerEquals(String ownerId) {
    if (ownerId == null || ownerId.isBlank()) {
      return null;
    }
    return (root, query, builder) -> builder.equal(root.get("ownerId"), UUID.fromString(ownerId));
  }

  private static Specification<FileMetadata> tagsLike(String tags) {
    if (tags == null || tags.isBlank()) {
      return null;
    }
    return (root, query, builder) -> builder.like(builder.lower(root.get("tags")),
        "%" + tags.toLowerCase() + "%");
  }

  private static Specification<FileMetadata> uploadedAfter(Instant after) {
    if (after == null) {
      return null;
    }
    return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("uploadTimestamp"), after);
  }

  private static Specification<FileMetadata> uploadedBefore(Instant before) {
    if (before == null) {
      return null;
    }
    return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("uploadTimestamp"), before);
  }
}
