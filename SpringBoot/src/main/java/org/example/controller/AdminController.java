package org.example.controller;

import jakarta.annotation.Resource;
import org.example.common.Result;
import org.example.entity.Admin;
import org.example.service.AdminService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping("/login")
    public Result login(@RequestBody Admin admin) {
        Admin result = adminService.login(admin.getUsername(), admin.getPassword());
        return Result.success(result);
    }

    @PostMapping("/register")
    public Result register(@RequestBody Admin admin) {
        adminService.register(admin);
        return Result.success();
    }

    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Map<String, String> map) {
        adminService.updatePassword(
            Integer.parseInt(map.get("id")),
            map.get("oldPassword"),
            map.get("newPassword")
        );
        return Result.success();
    }

    @PutMapping("/updateProfile")
    public Result updateProfile(@RequestBody Admin admin) {
        adminService.updateProfile(admin);
        return Result.success();
    }
}
