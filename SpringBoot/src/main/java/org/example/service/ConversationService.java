package org.example.service;

import jakarta.annotation.Resource;
import org.example.common.UserContext;
import org.example.entity.Conversation;
import org.example.mapper.ConversationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    @Resource
    private ConversationMapper conversationMapper;

    private Integer uid() { return UserContext.getUserId(); }

    public void save(String sessionId, String role, String content) {
        Conversation c = new Conversation();
        c.setUserId(uid());
        c.setSessionId(sessionId);
        c.setRole(role);
        c.setContent(content);
        conversationMapper.insert(c);
    }

    public void saveAll(String sessionId, List<Conversation> conversations) {
        for (Conversation c : conversations) {
            c.setUserId(uid());
            c.setSessionId(sessionId);
            conversationMapper.insert(c);
        }
    }

    public List<Conversation> loadHistory(String sessionId) {
        return conversationMapper.selectBySession(sessionId, uid());
    }

    public void clear(String sessionId) {
        conversationMapper.deleteBySession(sessionId, uid());
    }
}
