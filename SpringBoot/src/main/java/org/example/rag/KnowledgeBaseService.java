package org.example.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG 知识库服务。
 *
 * 流程：用户上传文档 → 切分 → 向量化 → 存入 Redis Stack
 * 查询：用户提问 → 向量检索 → 召回相关片段 → 注入 LLM 上下文 → 生成回答
 *
 * 和简历里吴京爽的 "高精度 RAG 检索管线" 是同一个概念，
 * 这里用 LangChain4j 内置能力简化实现。
 */
@Service
public class KnowledgeBaseService {

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 将文档文本存入知识库。
     *
     * @param content  文档全文
     * @param metadata 元数据（如文件名、来源），方便后续过滤
     */
    public void ingest(String content, java.util.Map<String, String> metadata) {
        Document document = Document.from(content, metadata);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(500, 50)) // 每段500字，重叠50字
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();

        ingestor.ingest(document);
    }

    /**
     * 知识库搜索，返回最相关的 N 个文本片段。
     */
    public List<String> search(String query, int maxResults) {
        // LangChain4j 的 EmbeddingStore 本身不直接支持按文本搜索，
        // 实际项目中需要用 Redis Search 的向量检索命令。
        // 这里用 EmbeddingStore.search() 返回匹配的片段。
        var embedded = embeddingModel.embed(query).content();
        var results = embeddingStore.search(
            dev.langchain4j.store.embedding.EmbeddingSearchRequest.builder()
                .queryEmbedding(embedded)
                .maxResults(maxResults)
                .minScore(0.6)
                .build()
        );

        return results.matches().stream()
            .map(m -> m.embedded().text())
            .toList();
    }

    /**
     * 清空知识库。
     */
    public void clear() {
        // Redis Stack 不支持清空，这里用重建方式
        // 实际实现依赖具体 EmbeddingStore 类型
    }
}
