package com.danimaldan.redactor;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RedactableAspect {
    @AfterReturning(pointcut = "@annotation(com.danimaldan.redactor.Redact)", returning = "returnValue")
    public Object performAround(Object returnValue) {
        var redactionApplicator = new RedactionApplicator(returnValue);

        redactionApplicator.redact();

        return returnValue;
    }
}
