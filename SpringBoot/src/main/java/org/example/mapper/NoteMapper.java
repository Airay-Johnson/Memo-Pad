package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Note;
import java.util.List;

@Mapper
public interface NoteMapper {
    List<Note> selectAll();
    List<Note> selectByGroupId(Integer groupId);
    void insertNewNote(Note note);
    void updateNote(Note note);
    void moveToTrash(Integer id);
    List<Note> selectTrash();
    void restoreNote(Integer id);
    void deleteForever(Integer id);
    Note selectById(@Param("id") Integer id);
    List<Note> search(@Param("keyword") String keyword);
    void updateGroupId(@Param("id") Integer id, @Param("groupId") Integer groupId);
}
