package org.example.controller;

import jakarta.annotation.Resource;
import org.example.common.Result;
import org.example.entity.Admin;
import org.example.service.AdminService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    AdminService adminService;

    @PostMapping("/login")
    public Result login(@RequestBody Admin admin) {
        Admin result = adminService.login(admin.getUsername(), admin.getPassword());
        if (result == null) {
            return Result.error("用户名或密码错误");
        }
        return Result.success(result);
    }

    @PostMapping("/register")
    public Result register(@RequestBody Admin admin) {
        Admin result = adminService.register(admin);
        if (result == null) {
            return Result.error("用户名已存在");
        }
        return Result.success(result);
    }

    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Admin admin) {
        adminService.updatePassword(admin.getId(), null, admin.getPassword());
        return Result.success();
    }

    @PutMapping("/updateProfile")
    public Result updateProfile(@RequestBody Admin admin) {
        adminService.updateProfile(admin);
        return Result.success();
    }
}
