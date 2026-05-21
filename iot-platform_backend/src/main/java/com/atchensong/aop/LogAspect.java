package com.atchensong.aop;

import com.atchensong.pojo.SystemLog;
import com.atchensong.service.SystemLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private SystemLogService systemLogService;

    @Pointcut("execution(* com.atchensong.controller..*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String className = signature.getDeclaringType().getSimpleName();
        Object[] args = joinPoint.getArgs();

        String logType = getLogType(methodName);
        String operator = "system";
        String content = String.format("[%s] %s.%s 耗时:%dms",
                logType, className, methodName, elapsed);

        SystemLog log = new SystemLog();
        log.setLogType(logType);
        log.setContent(content);
        log.setOperator(operator);
        log.setCreateTime(LocalDateTime.now());
        systemLogService.save(log);

        return result;
    }

    private String getLogType(String methodName) {
        String lower = methodName.toLowerCase();
        if (lower.contains("login")) return "登录";
        if (lower.contains("register")) return "设备注册";
        if (lower.contains("update") || lower.contains("edit")) return "修改";
        if (lower.contains("delete") || lower.contains("remove")) return "删除";
        if (lower.contains("list") || lower.contains("page")) return "查询";
        if (lower.contains("send")) return "指令下发";
        if (lower.contains("create") || lower.contains("add")) return "新增";
        return "其他";
    }
}
