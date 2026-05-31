package com.ragapp.service;

import com.ragapp.config.AppProperties;
import com.ragapp.model.Dto;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final AppProperties props;

    /**
     * Ingest raw text content directly.
     */
    public Dto.IngestResponse ingestText(String content, String source) {
        Document document = Document.from(content, Metadata.from("source", source != null ? source : "manual"));
        return ingestDocument(document);
    }

    /**
     * Ingest a plain-text or PDF file upload.
     */
    public Dto.IngestResponse ingestFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        log.info("Ingesting file: {}", filename);

        String text;
        try (InputStream is = file.getInputStream()) {
            if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
                text = extractPdfText(is);
            } else {
                text = new String(is.readAllBytes());
            }
        }

        Document document = Document.from(text, Metadata.from("source", filename));
        return ingestDocument(document);
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private Dto.IngestResponse ingestDocument(Document document) {
        AppProperties.Rag ragCfg = props.getRag();

        DocumentSplitter splitter = DocumentSplitters.recursive(
                ragCfg.getChunkSize(),
                ragCfg.getChunkOverlap()
        );

        List<TextSegment> segments = splitter.split(document);
        log.debug("Document split into {} segments", segments.size());

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);

        log.info("Stored {} chunks in Chroma", segments.size());
        return Dto.IngestResponse.of(segments.size());
    }

    private String extractPdfText(InputStream is) throws IOException {
        // Uses Apache PDFBox via langchain4j-document-parser-apache-pdfbox
        dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser parser =
                new dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser();
        return parser.parse(is).text();
    }
}
