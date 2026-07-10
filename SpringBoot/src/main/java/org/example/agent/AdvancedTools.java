package org.example.agent;

import dev.langchain4j.agent.tool.*;
import jakarta.annotation.Resource;
import org.example.entity.Note;
import org.example.service.NoteService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdvancedTools {

    @Resource
    private NoteService noteService;

    @Tool("批量删除多条笔记，移入回收站")
    public String batchDeleteNotes(@P("逗号分隔的笔记ID列表，如'1,3,5'") String noteIds) {
        List<Integer> ids = parseIds(noteIds);
        if (ids.isEmpty()) return "请提供有效的笔记ID列表";

        int count = 0;
        for (Integer id : ids) {
            Note note = noteService.selectById(id);
            if (note != null && (note.getDeleted() == null || note.getDeleted() == 0)) {
                noteService.moveToTrash(id);
                count++;
            }
        }
        return "已批量删除 " + count + " 条笔记";
    }

    @Tool("批量移动笔记到指定分组")
    public String batchMoveNotes(
        @P("逗号分隔的笔记ID列表") String noteIds,
        @P("目标分组ID") int groupId
    ) {
        List<Integer> ids = parseIds(noteIds);
        if (ids.isEmpty()) return "请提供有效的笔记ID列表";

        int count = 0;
        for (Integer id : ids) {
            Note note = noteService.selectById(id);
            if (note != null) {
                noteService.updateGroupId(id, groupId);
                count++;
            }
        }
        return "已批量移动 " + count + " 条笔记到分组 ID=" + groupId;
    }

    @Tool("获取笔记统计信息：总数、分组分布、时间分布")
    public String getNoteStats() {
        List<Note> notes = noteService.selectAll();
        long active = notes.stream().filter(n -> n.getDeleted() == null || n.getDeleted() == 0).count();
        long trashed = notes.stream().filter(n -> n.getDeleted() != null && n.getDeleted() == 1).count();

        var groupStats = notes.stream()
            .filter(n -> n.getGroupId() != null && (n.getDeleted() == null || n.getDeleted() == 0))
            .collect(Collectors.groupingBy(Note::getGroupId, Collectors.counting()));

        StringBuilder sb = new StringBuilder("笔记统计：\n");
        sb.append("- 正常笔记：").append(active).append(" 条\n");
        sb.append("- 回收站：").append(trashed).append(" 条\n");
        sb.append("- 总计：").append(notes.size()).append(" 条\n");
        if (!groupStats.isEmpty()) {
            sb.append("- 各分组笔记数：\n");
            groupStats.forEach((gid, cnt) ->
                sb.append("  分组 ID=").append(gid).append(": ").append(cnt).append(" 条\n"));
        }
        return sb.toString();
    }

    @Tool("将指定笔记导出为 Markdown 格式")
    public String exportNoteMarkdown(@P("笔记ID") int noteId) {
        Note note = noteService.selectById(noteId);
        if (note == null) return "笔记 ID=" + noteId + " 不存在";
        if (note.getDeleted() != null && note.getDeleted() == 1)
            return "笔记 ID=" + noteId + " 已在回收站中";

        StringBuilder md = new StringBuilder();
        md.append("# ").append(note.getTitle()).append("\n\n");
        md.append("> 更新时间: ").append(note.getUpdateTime()).append("\n\n");
        if (note.getContent() != null && !note.getContent().isEmpty()) {
            md.append(note.getContent()).append("\n");
        }
        return md.toString();
    }

    private List<Integer> parseIds(String ids) {
        if (ids == null || ids.trim().isEmpty()) return List.of();
        return Arrays.stream(ids.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }
}
