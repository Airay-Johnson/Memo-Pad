package org.example.service;

import jakarta.annotation.Resource;
import org.example.entity.Conversation;
import org.example.mapper.ConversationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    @Resource
    private ConversationMapper conversationMapper;

    /** 保存一条对话消息 */
    public void save(String sessionId, String role, String content) {
        Conversation c = new Conversation();
        c.setSessionId(sessionId);
        c.setRole(role);
        c.setContent(content);
        conversationMapper.insert(c);
    }

    /** 批量保存 */
    public void saveAll(String sessionId, List<Conversation> conversations) {
        for (Conversation c : conversations) {
            c.setSessionId(sessionId);
            conversationMapper.insert(c);
        }
    }

    /** 加载指定会话的全部历史 */
    public List<Conversation> loadHistory(String sessionId) {
        return conversationMapper.selectBySession(sessionId);
    }

    /** 清除旧会话（防止数据库膨胀） */
    public void clear(String sessionId) {
        conversationMapper.deleteBySession(sessionId);
    }
}
