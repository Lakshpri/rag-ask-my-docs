package com.ragapp.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class Dto {

    // ── Ingest ──────────────────────────────────────────────────────────────

    @Data
    public static class IngestTextRequest {
        @NotBlank(message = "content must not be blank")
        private String content;

        /** Optional metadata tag (e.g. source name, category) */
        private String source;
    }

    @Data
    public static class IngestResponse {
        private int chunksStored;
        private String message;

        public static IngestResponse of(int chunks) {
            IngestResponse r = new IngestResponse();
            r.chunksStored = chunks;
            r.message = chunks + " chunk(s) successfully ingested into Chroma.";
            return r;
        }
    }

    // ── Query ───────────────────────────────────────────────────────────────

    @Data
    public static class QueryRequest {
        @NotBlank(message = "question must not be blank")
        private String question;

        /** How many relevant chunks to retrieve (overrides config if set) */
        private Integer maxResults;
    }

    @Data
    public static class QueryResponse {
        private String answer;
        private java.util.List<SourceChunk> sources;
    }

    @Data
    public static class SourceChunk {
        private String text;
        private double score;
        private String source;
    }
}
