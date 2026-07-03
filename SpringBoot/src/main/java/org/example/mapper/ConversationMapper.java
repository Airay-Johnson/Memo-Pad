package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Conversation;
import java.util.List;

@Mapper
public interface ConversationMapper {
    void insert(Conversation conversation);
    List<Conversation> selectBySession(@Param("sessionId") String sessionId);
    void deleteBySession(@Param("sessionId") String sessionId);
}
