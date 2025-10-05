package com.example.usercenter.service;

import java.util.Set;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 检查用户是否拥有指定权限
     * @param userId 用户ID
     * @param permCode 权限编码
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permCode);

    /**
     * 检查用户是否拥有指定角色
     * @param userId 用户ID
     * @param roleName 角色名称
     * @return 是否拥有角色
     */
    boolean hasRole(Long userId, String roleName);

    /**
     * 获取用户拥有的所有权限编码
     * @param userId 用户ID
     * @return 权限编码集合
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 获取用户拥有的所有角色名称
     * @param userId 用户ID
     * @return 角色名称集合
     */
    Set<String> getUserRoles(Long userId);
}