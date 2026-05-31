package com.ragapp.controller;

import com.ragapp.model.Dto;
import com.ragapp.service.DocumentIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentIngestionService ingestionService;

    @PostMapping("/ingest")
    public ResponseEntity<Dto.IngestResponse> ingestText(
            @Valid @RequestBody Dto.IngestTextRequest request) {

        log.info("Ingesting text from source: {}", request.getSource());
        Dto.IngestResponse response = ingestionService.ingestText(
                request.getContent(), request.getSource());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Dto.IngestResponse> uploadFile(
            @RequestParam("file") MultipartFile file) throws Exception {

        log.info("Received file upload: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Dto.IngestResponse response = ingestionService.ingestFile(file);
        return ResponseEntity.ok(response);
    }
}
