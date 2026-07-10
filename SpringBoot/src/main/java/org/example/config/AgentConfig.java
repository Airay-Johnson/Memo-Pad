package org.example.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.Resource;
import org.example.agent.MemoAgent;
import org.example.agent.NoteTools;
import org.example.agent.AdvancedTools;
import org.example.guard.InputGuard;
import org.example.common.AiConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AgentConfig {

    @Resource
    private AiConfig aiConfig;

    @Resource
    private NoteTools noteTools;

    @Resource
    private AdvancedTools advancedTools;

    @Bean
    public ChatModel chatLanguageModel() {
        return OpenAiChatModel.builder()
            .apiKey(aiConfig.getApiKey())
            .modelName(aiConfig.getModel())
            .baseUrl(aiConfig.getApiUrl())
            .timeout(Duration.ofSeconds(60))
            .build();
    }

    @Bean
    public StreamingChatModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
            .apiKey(aiConfig.getApiKey())
            .modelName(aiConfig.getModel())
            .baseUrl(aiConfig.getApiUrl())
            .timeout(Duration.ofSeconds(120))
            .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public MemoAgent memoAgent(ChatModel chatModel, StreamingChatModel streamingChatModel) {
        return AiServices.builder(MemoAgent.class)
            .chatModel(chatModel)
            .streamingChatModel(streamingChatModel)
            .tools(noteTools, advancedTools)
            .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
            .inputGuardrails(new InputGuard())
            .build();
    }
}
