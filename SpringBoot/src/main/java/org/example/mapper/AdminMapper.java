package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.Admin;
import java.util.List;

@Mapper
public interface AdminMapper {
    List<Admin> selectAll();
    Admin selectByUsername(String username);
    void insert(Admin admin);
    void updatePassword(Integer id, Strin