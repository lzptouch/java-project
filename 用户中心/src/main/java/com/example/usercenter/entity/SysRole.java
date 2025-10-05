package com.example.usercenter.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * 角色实体类
 */
@Data
@Entity
@Table(name = "sys_role")
public class SysRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    @Column(name = "role_desc", length = 200)
    private String roleDesc;

    @CreationTimestamp
    @Column(name = "create_time")
    private Date createTime;

    // 多对多关系：一个角色可以有多个权限
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "sys_role_permission",
            joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "perm_id", referencedColumnName = "id")})
    private Set<SysPermission> permissions;

    // 多对多关系：一个角色可以被多个用户拥有（反向）
    @ManyToMany(mappedBy = "roles")
    private Set<SysUser> users;
}