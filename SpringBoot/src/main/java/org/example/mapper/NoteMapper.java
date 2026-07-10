package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Note;
import java.util.List;

@Mapper
public interface NoteMapper {
    List<Note> selectAll(@Param("userId") Integer userId);
    List<Note> selectByGroupId(@Param("groupId") Integer groupId, @Param("userId") Integer userId);
    void insertNewNote(Note note);
    void updateNote(Note note);
    void moveToTrash(@Param("id") Integer id, @Param("userId") Integer userId);
    List<Note> selectTrash(@Param("userId") Integer userId);
    void restoreNote(@Param("id") Integer id, @Param("userId") Integer userId);
    void deleteForever(@Param("id") Integer id, @Param("userId") Integer userId);
    Note selectById(@Param("id") Integer id, @Param("userId") Integer userId);
    List<Note> search(@Param("keyword") String keyword, @Param("userId") Integer userId);
    void updateGroupId(@Param("id") Integer id, @Param("groupId") Integer groupId, @Param("userId") Integer userId);
}
