package com.example.usercenter.service;

import com.example.usercenter.entity.SysUser;

/**
 * 用户服务接口
 */
public interface SysUserService {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户信息
     */
    SysUser findByUsername(String username);

    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建的用户
     */
    SysUser createUser(SysUser user);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新后的用户
     */
    SysUser updateUser(SysUser user);

    /**
     * 删除用户
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 校验用户密码
     * @param username 用户名
     * @param password 密码
     * @return 校验结果
     */
    boolean checkPassword(String username, String password);
}