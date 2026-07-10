package org.example.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    private String apiKey;
    private String apiUrl;
    private String model;

    public String getApiKey() {
        if (apiKey != null && !apiKey.isBlank()) return apiKey;
        return System.getenv("MEMOPAD_AI_API_KEY");
    }

    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}