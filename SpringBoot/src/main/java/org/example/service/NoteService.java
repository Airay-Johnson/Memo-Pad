package org.example.service;

import jakarta.annotation.Resource;
import org.example.common.UserContext;
import org.example.entity.Note;
import org.example.mapper.NoteMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NoteService {

    @Resource
    private NoteMapper noteMapper;

    private Integer uid() { return UserContext.getUserId(); }

    public List<Note> selectAll() { return noteMapper.selectAll(uid()); }

    public List<Note> selectByGroupId(Integer groupId) { return noteMapper.selectByGroupId(groupId, uid()); }

    public void insertNewNote(Note note) {
        note.setUserId(uid());
        noteMapper.insertNewNote(note);
    }

    public void updateNote(Note note) {
        note.setUserId(uid());
        noteMapper.updateNote(note);
    }

    public void moveToTrash(Integer id) { noteMapper.moveToTrash(id, uid()); }

    public List<Note> selectTrash() { return noteMapper.selectTrash(uid()); }

    public void restoreNote(Integer id) { noteMapper.restoreNote(id, uid()); }

    public void deleteForever(Integer id) { noteMapper.deleteForever(id, uid()); }

    public Note selectById(Integer id) { return noteMapper.selectById(id, uid()); }

    public List<Note> search(String keyword) { return noteMapper.search(keyword, uid()); }

    public void updateGroupId(Integer id, Integer groupId) { noteMapper.updateGroupId(id, groupId, uid()); }
}
