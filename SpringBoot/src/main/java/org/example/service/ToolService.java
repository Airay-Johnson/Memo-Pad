package org.example.service;

import jakarta.annotation.Resource;
import org.example.common.ToolResult;
import org.example.entity.Note;
import org.example.entity.NoteGroup;
import org.example.entity.ToolType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class ToolService {

    @Resource
    private NoteService noteService;

    @Resource
    private NoteGroupService noteGroupService;

    /**
     * 根据工具名执行对应的操作，返回结果文字。
     *
     * @param toolName AI 指定的工具名（必须和 ToolType 枚举值一致）
     * @param params   AI 传的参数，key=参数名，value=参数值
     */
    public ToolResult execute(String toolName, Map<String, String> params) {
        try {
            ToolType tool = ToolType.valueOf(toolName.toUpperCase());
            return switch (tool) {
                case LIST_NOTES   -> executeListNotes();
                case READ_NOTE    -> executeReadNote(params);
                case CREATE_NOTE  -> executeCreateNote(params);
                case UPDATE_NOTE  -> executeUpdateNote(params);
                case DELETE_NOTE  -> executeDeleteNote(params);
                case SEARCH_NOTE  -> executeSearchNote(params);
                case MOVE_NOTE    -> executeMoveNote(params);
                case LIST_GROUPS  -> executeListGroups();
            };
        } catch (IllegalArgumentException e) {
            return ToolResult.fail("未知的工具：" + toolName);
        } catch (Exception e) {
            return ToolResult.fail("工具执行失败：" + e.getMessage());
        }
    }

    // ---- 读取类工具 ----

    /** 列出所有笔记（标题 + ID + 更新时间） */
    private ToolResult executeListNotes() {
        List<Note> notes = noteService.selectAll();
        if (notes.isEmpty()) {
            return ToolResult.ok("当前没有笔记。");
        }
        StringBuilder sb = new StringBuilder("以下是你所有的笔记：\n");
        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            sb.append((i + 1)).append(". [ID:").append(n.getId())
              .append("] ").append(n.getTitle())
              .append("  (").append(n.getUpdateTime() != null ? n.getUpdateTime().toLocalDate() : "未知").append(")\n");
        }
        return ToolResult.ok(sb.toString());
    }

    /** 读取指定笔记的完整内容 */
    private ToolResult executeReadNote(Map<String, String> params) {
        String idStr = params.get("noteId");
        if (idStr == null) return ToolResult.fail("缺少参数 noteId");
        Note note = noteService.selectById(Integer.parseInt(idStr));
        if (note == null) return ToolResult.fail("笔记 ID=" + idStr + " 不存在");
        if (note.getDeleted() != null && note.getDeleted() == 1) {
            return ToolResult.fail("笔记 ID=" + idStr + " 已在回收站中");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("笔记标题：").append(note.getTitle()).append("\n");
        sb.append("内容：\n").append(note.getContent() != null ? note.getContent() : "(空)");
        if (note.getGroupId() != null) {
            sb.append("\n所在分组ID：").append(note.getGroupId());
        }
        return ToolResult.ok(sb.toString());
    }

    /** 按关键词搜索笔记 */
    private ToolResult executeSearchNote(Map<String, String> params) {
        String keyword = params.get("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            return ToolResult.fail("缺少参数 keyword");
        }
        List<Note> notes = noteService.search(keyword);
        if (notes.isEmpty()) {
            return ToolResult.ok("没有找到包含「" + keyword + "」的笔记。");
        }
        StringBuilder sb = new StringBuilder("搜索到 " + notes.size() + " 条包含「" + keyword + "」的笔记：\n");
        for (Note n : notes) {
            sb.append("- [ID:").append(n.getId()).append("] ").append(n.getTitle()).append("\n");
        }
        return ToolResult.ok(sb.toString());
    }

    /** 列出所有分组 */
    private ToolResult executeListGroups() {
        List<NoteGroup> groups = noteGroupService.selectAll();
        if (groups.isEmpty()) {
            return ToolResult.ok("当前没有分组。");
        }
        StringBuilder sb = new StringBuilder("以下是你所有的分组：\n");
        for (NoteGroup g : groups) {
            sb.append("- [ID:").append(g.getId()).append("] ").append(g.getName()).append("\n");
        }
        return ToolResult.ok(sb.toString());
    }

    // ---- 写入类工具 ----

    /** 创建一条新笔记 */
    private ToolResult executeCreateNote(Map<String, String> params) {
        String title = params.get("title");
        String content = params.getOrDefault("content", "");
        if (title == null || title.trim().isEmpty()) {
            return ToolResult.fail("缺少参数 title");
        }
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        noteService.insertNewNote(note);
        return ToolResult.ok("笔记「" + title + "」创建成功，ID=" + note.getId());
    }

    /** 修改笔记的标题或内容 */
    private ToolResult executeUpdateNote(Map<String, String> params) {
        String idStr = params.get("noteId");
        if (idStr == null) return ToolResult.fail("缺少参数 noteId");

        Note note = noteService.selectById(Integer.parseInt(idStr));
        if (note == null) return ToolResult.fail("笔记 ID=" + idStr + " 不存在");
        if (note.getDeleted() != null && note.getDeleted() == 1) {
            return ToolResult.fail("笔记 ID=" + idStr + " 已在回收站中，无法修改");
        }

        String newTitle = params.get("title");
        String newContent = params.get("content");
        if (newTitle != null) note.setTitle(newTitle);
        if (newContent != null) note.setContent(newContent);

        noteService.updateNote(note);
        return ToolResult.ok("笔记「" + note.getTitle() + "」已更新");
    }

    /** 删除笔记（移至回收站） */
    private ToolResult executeDeleteNote(Map<String, String> params) {
        String idStr = params.get("noteId");
        if (idStr == null) return ToolResult.fail("缺少参数 noteId");

        Note note = noteService.selectById(Integer.parseInt(idStr));
        if (note == null) return ToolResult.fail("笔记 ID=" + idStr + " 不存在");

        noteService.moveToTrash(Integer.parseInt(idStr));
        return ToolResult.ok("笔记「" + note.getTitle() + "」已移至回收站");
    }

    /** 把笔记移动到某个分组 */
    private ToolResult executeMoveNote(Map<String, String> params) {
        String noteIdStr = params.get("noteId");
        String groupIdStr = params.get("groupId");
        if (noteIdStr == null) return ToolResult.fail("缺少参数 noteId");
        if (groupIdStr == null) return ToolResult.fail("缺少参数 groupId");

        Note note = noteService.selectById(Integer.parseInt(noteIdStr));
        if (note == null) return ToolResult.fail("笔记 ID=" + noteIdStr + " 不存在");

        noteService.updateGroupId(Integer.parseInt(noteIdStr), Integer.parseInt(groupIdStr));
        return ToolResult.ok("笔记「" + note.getTitle() + "」已移动到分组 ID=" + groupIdStr);
    }
}
