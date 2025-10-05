package com.example.usercenter.controller;

import com.example.usercenter.annotation.RequirePermission;
import com.example.usercenter.entity.SysUser;
import com.example.usercenter.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 处理用户的创建、更新和删除等管理操作
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {

    @Autowired
    private SysUserService userService;

    /**
     * 创建用户
     * 需要user:create权限
     */
    @PostMapping
    @RequirePermission("user:create")
    public SysUser createUser(@RequestBody SysUser user) {
        return userService.createUser(user);
    }

    /**
     * 更新用户信息
     * 需要user:update权限
     */
    @PutMapping("/{id}")
    @RequirePermission("user:update")
    public SysUser updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        return userService.updateUser(user);
    }

    /**
     * 删除用户
     * 需要user:delete权限
     */
    @DeleteMapping("/{id}")
    @RequirePermission("user:delete")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    /**
     * 根据用户名查找用户
     * 需要user:view权限
     */
    @GetMapping("/by-username/{username}")
    @RequirePermission("user:view")
    public SysUser findByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }
}