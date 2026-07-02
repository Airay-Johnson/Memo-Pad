package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.Admin;

@Mapper
public interface AdminMapper {
    Admin selectByUsername(String username);
    Admin selectById(Integer id);
    void insert(Admin admin);
    void updatePassword(Admin admin);
    v