package org.example.service;

import jakarta.annotation.Resource;
import org.example.entity.Note;
import org.example.mapper.NoteMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NoteService {

    @Resource
    private NoteMapper noteMapper;

    public List<Note> selectAll() { return noteMapper.selectAll(); }

    public List<Note> selectByGroupId(Integer groupId) { return noteMapper.selectByGroupId(groupId); }

    public void insertNewNote(Note note) { noteMapper.insertNewNote(note); }

    public void updateNote(Note note) { noteMapper.updateNote(note); }

    public void moveToTrash(Integer id) { noteMapper.moveToTrash(id); }

    public List<Note> selectTrash() { return noteMapper.selectTrash(); }

    public void restoreNote(Integer id) { noteMapper.restoreNote(id); }

    public void deleteForever(Integer id) { noteMapper.deleteForever(id); }

    public Note selectById(Integer id) { return noteMapper.selectById(id); }

    public List<Note> search(String keyword) { return noteMapper.search(keyword); }

    public void updateGroupId(Integer id, Integer groupId) { noteMapper.updateGroupId(id, groupId); }
}
