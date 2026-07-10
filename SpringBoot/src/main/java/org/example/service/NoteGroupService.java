package org.example.service;

import jakarta.annotation.Resource;
import org.example.common.UserContext;
import org.example.entity.NoteGroup;
import org.example.mapper.NoteGroupMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NoteGroupService {

    @Resource
    private NoteGroupMapper noteGroupMapper;

    private Integer uid() { return UserContext.getUserId(); }

    public List<NoteGroup> selectAll() { return noteGroupMapper.selectAll(uid()); }

    public void insert(NoteGroup noteGroup) {
        noteGroup.setUserId(uid());
        noteGroupMapper.insert(noteGroup);
    }

    public void deleteGroup(Integer id) { noteGroupMapper.deleteGroup(id, uid()); }
}
