package com.example.usercenter.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 权限实体类
 */
@Data
@Entity
@Table(name = "sys_permission")
public class SysPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "perm_code", nullable = false, unique = true, length = 100)
    private String permCode;

    @Column(name = "perm_name", nullable = false, length = 100)
    private String permName;

    @Column(name = "resource_type", length = 20)
    private String resourceType;

    @Column(name = "parent_id")
    private Long parentId;

    // 多对多关系：一个权限可以被多个角色拥有（反向）
    @ManyToMany(mappedBy = "permissions")
    private Set<SysRole> roles;
}