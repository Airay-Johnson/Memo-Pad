package org.example.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.Resource;
import org.example.agent.MemoAgent;
import org.example.agent.NoteTools;
import org.example.common.AiConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 组件装配工厂 — 替代旧版手写注入。
 */
@Configuration
public class AgentConfig {

    @Resource
    private AiConfig aiConfig;

    @Resource
    private NoteTools noteTools;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return QwenChatModel.builder()
            .apiKey(aiConfig.getApiKey())
            .modelName(aiConfig.getModel())
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
    public MemoAgent memoAgent(ChatLanguageModel chatModel) {
        return AiServices.builder(MemoAgent.class)
            .chatLanguageModel(chatModel)
            .tools(noteTools)
            .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
            .build();
    }
}
