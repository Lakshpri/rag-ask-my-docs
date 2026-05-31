package com.ragapp.controller;

import com.ragapp.model.Dto;
import com.ragapp.service.RagQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
public class QueryController {

    private final RagQueryService ragQueryService;

    /**
     * POST /api/query
     * Ask a question; the service retrieves relevant chunks and generates a Claude answer.
     */
    @PostMapping
    public ResponseEntity<Dto.QueryResponse> query(
            @Valid @RequestBody Dto.QueryRequest request) {

        log.info("Received question: {}", request.getQuestion());
        Dto.QueryResponse response = ragQueryService.query(
                request.getQuestion(), request.getMaxResults());
        return ResponseEntity.ok(response);
    }
}
