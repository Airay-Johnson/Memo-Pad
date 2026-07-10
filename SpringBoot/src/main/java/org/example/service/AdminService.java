package org.example.service;

import jakarta.annotation.Resource;
import org.example.entity.Admin;
import org.example.mapper.AdminMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Admin login(String email, String password) {
        Admin admin = adminMapper.selectByUsername(email);
        if (admin == null) throw new RuntimeException("用户不存在");
        if (!encoder.matches(password, admin.getPassword())) throw new RuntimeException("密码错误");
        admin.setPassword(null);
        return admin;
    }

    public Admin register(Admin admin) {
        Admin exist = adminMapper.selectByUsername(admin.getEmail());
        if (exist != null) throw new RuntimeException("该邮箱已注册");
        if (admin.getName() == null || admin.getName().isEmpty()) {
            String email = admin.getEmail();
            admin.setName(email.substring(0, email.indexOf('@')));
        }
        admin.setPassword(encoder.encode(admin.getPassword()));
        adminMapper.insert(admin);
        admin.setPassword(null);
        return admin;
    }

    public void updatePassword(Integer id, String oldPassword, String newPassword) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) throw new RuntimeException("用户不存在");
        if (!encoder.matches(oldPassword, admin.getPassword())) throw new RuntimeException("原密码错误");
        Admin update = new Admin();
        update.setId(id);
        update.setPassword(encoder.encode(newPassword));
        adminMapper.updatePassword(update);
    }

    public void updateProfile(Admin admin) {
        adminMapper.updateProfile(admin);
    }

    public Admin selectById(Integer id) {
        return adminMapper.selectById(id);
    }
}
