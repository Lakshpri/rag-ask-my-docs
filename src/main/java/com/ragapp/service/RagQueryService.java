package com.ragapp.service;

import com.ragapp.config.AppProperties;
import com.ragapp.model.Dto;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagQueryService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatLanguageModel chatModel;
    private final AppProperties props;

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant that answers questions strictly based on the provided context.
            If the context does not contain enough information to answer the question, say so clearly.
            Do not make up information. Always be concise and accurate.
            """;

    public Dto.QueryResponse query(String question, Integer maxResultsOverride) {
        AppProperties.Rag ragCfg = props.getRag();
        int maxResults = maxResultsOverride != null ? maxResultsOverride : ragCfg.getMaxResults();

        // 1. Embed the question
        log.debug("Embedding question: {}", question);
        Embedding questionEmbedding = embeddingModel.embed(question).content();

        // 2. Retrieve relevant chunks from Chroma
        List<EmbeddingMatch<TextSegment>> matches =
                embeddingStore.findRelevant(questionEmbedding, maxResults, ragCfg.getMinScore());

        log.info("Retrieved {} relevant chunks for question", matches.size());

        if (matches.isEmpty()) {
            Dto.QueryResponse resp = new Dto.QueryResponse();
            resp.setAnswer("I could not find any relevant information in the knowledge base to answer your question.");
            resp.setSources(List.of());
            return resp;
        }

        // 3. Build context from retrieved chunks
        String context = matches.stream()
                .map(m -> m.embedded().text())
                .collect(Collectors.joining("\n\n---\n\n"));

        // 4. Build the prompt
        String userPrompt = """
                Context:
                %s
                
                Question: %s
                
                Answer:
                """.formatted(context, question);

        // 5. Call Claude
        log.debug("Calling Claude with context ({} chars)", context.length());
        Response<AiMessage> response = chatModel.generate(
                dev.langchain4j.data.message.SystemMessage.from(SYSTEM_PROMPT),
                UserMessage.from(userPrompt)
        );

        // 6. Map to response DTO
        Dto.QueryResponse queryResponse = new Dto.QueryResponse();
        queryResponse.setAnswer(response.content().text());
        queryResponse.setSources(
                matches.stream().map(match -> {
                    Dto.SourceChunk chunk = new Dto.SourceChunk();
                    chunk.setText(match.embedded().text());
                    chunk.setScore(match.score());
                    String src = match.embedded().metadata().getString("source");
                    chunk.setSource(src != null ? src : "unknown");
                    return chunk;
                }).collect(Collectors.toList())
        );

        return queryResponse;
    }
}
