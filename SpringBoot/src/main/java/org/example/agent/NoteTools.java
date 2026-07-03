package org.example.agent;

import dev.langchain4j.agent.tool.*;
import jakarta.annotation.Resource;
import org.example.common.ToolResult;
import org.example.entity.Note;
import org.example.entity.NoteGroup;
import org.example.service.NoteService;
import org.example.service.NoteGroupService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LangChain4j 版工具类 — 用 @Tool 注解替代旧版 ToolService 的 switch 路由。
 *
 * 每个 public 方法加 @Tool("描述") → 框架自动：
 * 1. 生成工具描述给 AI
 * 2. 从方法签名提取参数名和类型
 * 3. 路由 AI 的工具调用请求
 */
@Component
public class NoteTools {

    @Resource
    private NoteService noteService;

    @Resource
    private NoteGroupService noteGroupService;

    @Tool("查看所有笔记的列表，返回标题和ID")
    public String listAllNotes() {
        List<Note> notes = noteService.selectAll();
        if (notes.isEmpty()) return "当前没有笔记。";

        StringBuilder sb = new StringBuilder("所有笔记：\n");
        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            sb.append(i + 1).append(". [ID:").append(n.getId())
              .append("] ").append(n.getTitle())
              .append(" (").append(n.getUpdateTime() != null ? n.getUpdateTime().toLocalDate() : "未知").append(")\n");
        }
        return sb.toString();
    }

    @Tool("读取指定笔记的完整内容")
    public String readNote(@P("笔记ID") int noteId) {
        Note note = noteService.selectById(noteId);
        if (note == null) return "笔记 ID=" + noteId + " 不存在";
        if (note.getDeleted() != null && note.getDeleted() == 1)
            return "笔记 ID=" + noteId + " 已在回收站中";

        StringBuilder sb = new StringBuilder();
        sb.append("标题：").append(note.getTitle()).append("\n");
        sb.append("内容：\n").append(note.getContent() != null ? note.getContent() : "(空)");
        if (note.getGroupId() != null) sb.append("\n所在分组ID：").append(note.getGroupId());
        return sb.toString();
    }

    @Tool("创建一条新笔记")
    public String createNote(
        @P("笔记标题") String title,
        @P("笔记内容，可空") String content
    ) {
        if (title == null || title.trim().isEmpty()) return "标题不能为空";
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content != null ? content : "");
        noteService.insertNewNote(note);
        return "笔记「" + title + "」创建成功，ID=" + note.getId();
    }

    @Tool("修改已有笔记的标题或内容")
    public String updateNote(
        @P("笔记ID") int noteId,
        @P("新标题，不传则不改") String title,
        @P("新内容，不传则不改") String content
    ) {
        Note note = noteService.selectById(noteId);
        if (note == null) return "笔记 ID=" + noteId + " 不存在";
        if (note.getDeleted() != null && note.getDeleted() == 1)
            return "笔记 ID=" + noteId + " 已在回收站中，无法修改";

        if (title != null && !title.trim().isEmpty()) note.setTitle(title);
        if (content != null) note.setContent(content);
        noteService.updateNote(note);
        return "笔记「" + note.getTitle() + "」已更新";
    }

    @Tool("删除笔记（移入回收站）")
    public String deleteNote(@P("笔记ID") int noteId) {
        Note note = noteService.selectById(noteId);
        if (note == null) return "笔记 ID=" + noteId + " 不存在";
        noteService.moveToTrash(noteId);
        return "笔记「" + note.getTitle() + "」已移至回收站";
    }

    @Tool("按关键词搜索笔记，匹配标题和内容")
    public String searchNotes(@P("搜索关键词") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return "请输入搜索关键词";
        List<Note> notes = noteService.search(keyword);
        if (notes.isEmpty()) return "没有找到包含「" + keyword + "」的笔记。";

        StringBuilder sb = new StringBuilder("搜索到 " + notes.size() + " 条笔记：\n");
        for (Note n : notes)
            sb.append("- [ID:").append(n.getId()).append("] ").append(n.getTitle()).append("\n");
        return sb.toString();
    }

    @Tool("把笔记移动到指定分组")
    public String moveNote(
        @P("笔记ID") int noteId,
        @P("分组ID") int groupId
    ) {
        Note note = noteService.selectById(noteId);
        if (note == null) return "笔记 ID=" + noteId + " 不存在";
        noteService.updateGroupId(noteId, groupId);
        return "笔记「" + note.getTitle() + "」已移动到分组 ID=" + groupId;
    }

    @Tool("查看所有分组")
    public String listAllGroups() {
        List<NoteGroup> groups = noteGroupService.selectAll();
        if (groups.isEmpty()) return "当前没有分组。";

        StringBuilder sb = new StringBuilder("所有分组：\n");
        for (NoteGroup g : groups)
            sb.append("- [ID:").append(g.getId()).append("] ").append(g.getName()).append("\n");
        return sb.toString();
    }
}
