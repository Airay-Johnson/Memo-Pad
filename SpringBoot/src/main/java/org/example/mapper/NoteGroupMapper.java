package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.NoteGroup;
import java.util.List;

@Mapper
public interface NoteGroupMapper {
    List<NoteGroup> selectAll(@Param("userId") Integer userId);
    void insert(NoteGroup noteGroup);
    void deleteGroup(@Param("id") Integer id, @Param("userId") Integer userId);
}
