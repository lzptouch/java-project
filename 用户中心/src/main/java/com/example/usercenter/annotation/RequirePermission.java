package com.example.usercenter.annotation;

import java.lang.annotation.*;

/**
 * 自定义权限验证注解
 * 用于标记需要特定权限才能访问的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 需要的权限编码
     */
    String value();

    /**
     * 权限验证失败的提示信息
     */
    String message() default "权限不足，无法访问该资源";
}