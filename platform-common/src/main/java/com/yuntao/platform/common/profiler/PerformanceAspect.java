package com.yuntao.platform.common.profiler;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class PerformanceAspect {

    @Around("execution(* com..service.impl.*.*(..)) ")
    public Object executePerLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String clsName = method.getDeclaringClass().getName();
        String methodName = method.getName();
        String actionMsg = clsName + "." + methodName;
        //content
        Class<?>[] parameterTypes = method.getParameterTypes();
//        StringBuilder sb = new StringBuilder();
        List<String> contentList = new ArrayList<>();
        for (Class<?> parameterType : parameterTypes) {
            contentList.add(parameterType.getName());
//            sb.append("|");
//            sb.append(parameterType.getName());
        }
//        String content =  null;
//        if(sb.length() > 0){
//            content = sb.substring(1);
//        }
        //end
        ProfileTaskManger.start(actionMsg, contentList.toString());
        try {
            return joinPoint.proceed();
        } finally {
            ProfileTaskManger.end();
        }
    }
}
