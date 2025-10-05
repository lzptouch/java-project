package com.example.usercenter.controller;

import com.example.usercenter.annotation.RequirePermission;
import com.example.usercenter.entity.SysUser;
import com.example.usercenter.repository.SysUserRepository;
import com.example.usercenter.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 用户控制器
 * 处理用户相关请求
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取当前登录用户信息
     * 不需要特定权限
     */
    @GetMapping("/current")
    public SysUser getCurrentUser() {
        // 从Security上下文中获取用户ID
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 根据ID查找用户信息
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 获取当前用户的权限列表
     * 需要user:view权限
     */
    @GetMapping("/permissions")
    @RequirePermission("user:view")
    public Set<String> getUserPermissions() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return permissionService.getUserPermissions(userId);
    }

    /**
     * 获取当前用户的角色列表
     * 需要user:view权限
     */
    @GetMapping("/roles")
    @RequirePermission("user:view")
    public Set<String> getUserRoles() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return permissionService.getUserRoles(userId);
    }
}