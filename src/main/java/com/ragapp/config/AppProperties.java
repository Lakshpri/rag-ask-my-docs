package com.ragapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Anthropic anthropic = new Anthropic();
    private Chroma chroma = new Chroma();
    private Rag rag = new Rag();

    @Data
    public static class Anthropic {
        private String apiKey;
        private String modelName = "claude-3-5-sonnet-20241022";
        private int maxTokens = 1024;
        private double temperature = 0.7;
    }

    @Data
    public static class Chroma {
        private String url = "http://localhost:8000";
        private String collectionName = "documents";
    }

    @Data
    public static class Rag {
        private int maxResults = 5;
        private double minScore = 0.6;
        private int chunkSize = 500;
        private int chunkOverlap = 50;
    }
}
