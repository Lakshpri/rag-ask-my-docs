package com.ragapp.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LangChainConfig {

    @Value("${groq.api-key}")
    private String groqApiKey;

    @Value("${groq.model-name:llama3-8b-8192}")
    private String groqModelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        log.info("Initialising Groq model: {}", groqModelName);
        return OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(groqApiKey)
                .modelName(groqModelName)
                .maxTokens(1024)
                .temperature(0.7)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Loading AllMiniLmL6V2 embedding model (local)");
        return new AllMiniLmL6V2QuantizedEmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> chromaEmbeddingStore() {
        log.info("Using in-memory embedding store");
        return new InMemoryEmbeddingStore<>();
    }
}
