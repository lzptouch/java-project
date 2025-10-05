package com.example.usercenter.init;

import com.example.usercenter.entity.SysPermission;
import com.example.usercenter.entity.SysRole;
import com.example.usercenter.entity.SysUser;
import com.example.usercenter.repository.SysPermissionRepository;
import com.example.usercenter.repository.SysRoleRepository;
import com.example.usercenter.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 数据初始化类
 * 在系统启动时初始化测试数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private SysRoleRepository roleRepository;

    @Autowired
    private SysPermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 初始化权限
        initPermissions();

        // 初始化角色
        initRoles();

        // 初始化用户
        initUsers();
    }

    /**
     * 初始化权限
     */
    private void initPermissions() {
        // 检查权限是否已存在
        if (permissionRepository.count() == 0) {
            // 创建用户相关权限
            SysPermission userViewPermission = new SysPermission();
            userViewPermission.setPermCode("user:view");
            userViewPermission.setPermName("查看用户");
            userViewPermission.setResourceType("menu");
            permissionRepository.save(userViewPermission);

            SysPermission userCreatePermission = new SysPermission();
            userCreatePermission.setPermCode("user:create");
            userCreatePermission.setPermName("创建用户");
            userCreatePermission.setResourceType("button");
            permissionRepository.save(userCreatePermission);

            SysPermission userUpdatePermission = new SysPermission();
            userUpdatePermission.setPermCode("user:update");
            userUpdatePermission.setPermName("更新用户");
            userUpdatePermission.setResourceType("button");
            permissionRepository.save(userUpdatePermission);

            SysPermission userDeletePermission = new SysPermission();
            userDeletePermission.setPermCode("user:delete");
            userDeletePermission.setPermName("删除用户");
            userDeletePermission.setResourceType("button");
            permissionRepository.save(userDeletePermission);

            // 创建角色相关权限
            SysPermission roleViewPermission = new SysPermission();
            roleViewPermission.setPermCode("role:view");
            roleViewPermission.setPermName("查看角色");
            roleViewPermission.setResourceType("menu");
            permissionRepository.save(roleViewPermission);

            SysPermission roleCreatePermission = new SysPermission();
            roleCreatePermission.setPermCode("role:create");
            roleCreatePermission.setPermName("创建角色");
            roleCreatePermission.setResourceType("button");
            permissionRepository.save(roleCreatePermission);
        }
    }

    /**
     * 初始化角色
     */
    private void initRoles() {
        // 检查角色是否已存在
        if (roleRepository.count() == 0) {
            // 创建管理员角色
            SysRole adminRole = new SysRole();
            adminRole.setRoleName("ADMIN");
            adminRole.setRoleDesc("系统管理员");
            
            // 获取所有权限
            Set<SysPermission> adminPermissions = new HashSet<>(permissionRepository.findAll());
            adminRole.setPermissions(adminPermissions);
            
            roleRepository.save(adminRole);

            // 创建普通用户角色
            SysRole userRole = new SysRole();
            userRole.setRoleName("USER");
            userRole.setRoleDesc("普通用户");
            
            // 获取部分权限
            Set<SysPermission> userPermissions = new HashSet<>();
            userPermissions.add(permissionRepository.findByPermCode("user:view").orElse(null));
            userRole.setPermissions(userPermissions);
            
            roleRepository.save(userRole);
        }
    }

    /**
     * 初始化用户
     */
    private void initUsers() {
        // 检查用户是否已存在
        if (userRepository.count() == 0) {
            // 创建管理员用户
            SysUser adminUser = new SysUser();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setNickname("系统管理员");
            adminUser.setStatus(1);
            
            // 设置角色
            Set<SysRole> adminRoles = new HashSet<>();
            adminRoles.add(roleRepository.findByRoleName("ADMIN").orElse(null));
            adminUser.setRoles(adminRoles);
            
            userRepository.save(adminUser);

            // 创建普通用户
            SysUser normalUser = new SysUser();
            normalUser.setUsername("user");
            normalUser.setPassword(passwordEncoder.encode("user123"));
            normalUser.setNickname("普通用户");
            normalUser.setStatus(1);
            
            // 设置角色
            Set<SysRole> userRoles = new HashSet<>();
            userRoles.add(roleRepository.findByRoleName("USER").orElse(null));
            normalUser.setRoles(userRoles);
            
            userRepository.save(normalUser);
        }
    }
}