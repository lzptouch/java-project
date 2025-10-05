package com.example.usercenter.service.impl;

import com.example.usercenter.entity.SysPermission;
import com.example.usercenter.entity.SysRole;
import com.example.usercenter.entity.SysUser;
import com.example.usercenter.repository.SysUserRepository;
import com.example.usercenter.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * 权限服务实现类
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private SysUserRepository userRepository;

    @Override
    public boolean hasPermission(Long userId, String permCode) {
        SysUser user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        // 检查用户是否拥有指定权限
        for (SysRole role : user.getRoles()) {
            for (SysPermission permission : role.getPermissions()) {
                if (permCode.equals(permission.getPermCode())) {
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public boolean hasRole(Long userId, String roleName) {
        SysUser user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        // 检查用户是否拥有指定角色
        for (SysRole role : user.getRoles()) {
            if (roleName.equals(role.getRoleName())) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public Set<String> getUserPermissions(Long userId) {
        Set<String> permissions = new HashSet<>();
        
        SysUser user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return permissions;
        }
        
        // 获取用户的所有权限编码
        for (SysRole role : user.getRoles()) {
            for (SysPermission permission : role.getPermissions()) {
                permissions.add(permission.getPermCode());
            }
        }
        
        return permissions;
    }

    @Override
    public Set<String> getUserRoles(Long userId) {
        Set<String> roles = new HashSet<>();
        
        SysUser user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return roles;
        }
        
        // 获取用户的所有角色名称
        for (SysRole role : user.getRoles()) {
            roles.add(role.getRoleName());
        }
        
        return roles;
    }
}