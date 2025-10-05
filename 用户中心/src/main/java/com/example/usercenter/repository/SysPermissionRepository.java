package com.example.usercenter.repository;

import com.example.usercenter.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 权限数据访问接口
 */
@Repository
public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {

    /**
     * 根据权限编码查找权限
     * @param permCode 权限编码
     * @return 权限信息
     */
    Optional<SysPermission> findByPermCode(String permCode);

    /**
     * 检查权限编码是否已存在
     * @param permCode 权限编码
     * @return 是否存在
     */
    boolean existsByPermCode(String permCode);
}