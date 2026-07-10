package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Admin;

@Mapper
public interface AdminMapper {
    Admin selectByUsername(@Param("email") String email);
    Admin selectById(@Param("id") Integer id);
    void insert(Admin admin);
    void updatePassword(Admin admin);
    void updateProfile(Admin admin);
}
