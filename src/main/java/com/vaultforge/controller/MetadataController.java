package com.vaultforge.controller;

import com.vaultforge.dto.metadata.MetadataResponse;
import com.vaultforge.dto.metadata.MetadataSearchRequest;
import com.vaultforge.service.MetadataService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/metadata")
public class MetadataController {
  private final MetadataService metadataService;

  public MetadataController(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @GetMapping("/all")
  public ResponseEntity<List<MetadataResponse>> listAll() {
    return ResponseEntity.ok(metadataService.listAll());
  }

  @PostMapping("/search")
  public ResponseEntity<Page<MetadataResponse>> search(
      @RequestBody MetadataSearchRequest request,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "50") int size,
      @RequestParam(value = "sortBy", defaultValue = "uploadTimestamp") String sortBy,
      @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    return ResponseEntity.ok(metadataService.search(request, PageRequest.of(page, size, sort)));
  }
}
