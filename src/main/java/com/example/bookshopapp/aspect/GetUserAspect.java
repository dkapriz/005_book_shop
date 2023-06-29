package com.example.bookshopapp.aspect;

import com.example.bookshopapp.model.User;
import com.example.bookshopapp.service.AuthService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class GetUserAspect {

    private final AuthService authService;

    @Autowired
    public GetUserAspect(AuthService authService) {
        this.authService = authService;
    }

    @Pointcut("@annotation(CurrentSecurityUser) && args(..,user)")
    public void getCurrentSecurityUser(User user) {
    }

    @Around(value = "getCurrentSecurityUser(user)", argNames = "proceedingJoinPoint,user")
    public Object execAdviceForGetCurrentUser(ProceedingJoinPoint proceedingJoinPoint, User user) throws Throwable {
        User curUser = authService.getCurrentUser();
        proceedingJoinPoint.getArgs()[proceedingJoinPoint.getArgs().length - 1] = curUser;
        return proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
    }
}
