package com.example.bookshopapp.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggerMethodAspect {
    @Around("@annotation(com.example.bookshopapp.aspect.LoggingMethod)")
    public Object execAdviceLogMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        log.info(String.format(
                "%s (%s) : %s in %s[ms]",
                ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod().getName(),
                Arrays.toString(proceedingJoinPoint.getArgs()),
                result,
                System.currentTimeMillis() - start
        ));
        return result;
    }
}
