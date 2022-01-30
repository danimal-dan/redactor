package com.danimaldan.redactor;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RedactableAspect {
    @Around("@annotation(com.danimaldan.redactor.Redact)")
    public Object performAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object resultObject = joinPoint.proceed();

        var redactionApplicator = new RedactionApplicator(resultObject);

        redactionApplicator.redact();

        return resultObject;
    }
}
