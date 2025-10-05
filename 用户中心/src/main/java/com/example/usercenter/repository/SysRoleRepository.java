package com.example.usercenter.repository;

import com.example.usercenter.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色数据访问接口
 */
@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    /**
     * 根据角色名称查找角色
     * @param roleName 角色名称
     * @return 角色信息
     */
    Optional<SysRole> findByRoleName(String roleName);

    /**
     * 检查角色名称是否已存在
     * @param roleName 角色名称
     * @return 是否存在
     */
    boolean existsByRoleName(String roleName);
}