package com.example.usercenter.aspect;

import com.example.usercenter.annotation.RequirePermission;
import com.example.usercenter.service.PermissionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 权限验证切面
 * 用于拦截并验证标记了@RequirePermission注解的方法调用
 */
@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private PermissionService permissionService;

    // 定义切点：拦截所有标记了@RequirePermission注解的方法
    @Pointcut("@annotation(com.example.usercenter.annotation.RequirePermission)")
    public void permissionPointcut() {
    }

    // 环绕通知：在方法执行前后进行权限验证
    @Around("permissionPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前用户ID
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取@RequirePermission注解
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        String requiredPermission = annotation.value();
        String message = annotation.message();

        // 验证权限
        if (!permissionService.hasPermission(userId, requiredPermission)) {
            throw new RuntimeException(message);
        }

        // 权限验证通过，执行原方法
        return joinPoint.proceed();
    }
}