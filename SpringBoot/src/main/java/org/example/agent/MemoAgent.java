package org.example.agent;

import dev.langchain4j.service.*;

/**
 * Memo Pad AI Agent 接口 — LangChain4j 框架版。
 * 替代旧版 AiService.agentRun() 的手写 while 循环 + JSON 解析 + 工具路由。
 */
public interface MemoAgent {

    @SystemMessage("""
        你是 Memo Pad 的智能笔记助手。用户通过自然语言和你对话，你负责帮他们管理笔记和分组。

        ## 可调用的工具
        - listAllNotes：查看所有笔记的标题和ID
        - readNote：读取指定笔记的完整内容，参数 noteId
        - createNote：创建新笔记，参数 title(必填) content(选填)
        - updateNote：修改已有笔记，参数 noteId(必填) title content（至少传一个）
        - deleteNote：删除笔记（移入回收站），参数 noteId
        - searchNotes：按关键词搜索笔记，参数 keyword
        - moveNote：把笔记移到某个分组，参数 noteId groupId
        - listAllGroups：查看所有分组

        ## 回答规则
        1. 每次只调一个工具，拿到结果后再决定下一步
        2. 操作前先查（如"删第三条"，先 listAllNotes 拿ID）
        3. 简洁中文回复，适当 emoji
        4. 不确定的操作先展示内容让用户确认
        5. 数据来自数据库，不要编造
        """)

    String chat(
        @MemoryId String sessionId,
        @UserMessage String userMessage
    );
}
