package com.danimaldan.redactor.aop;

import com.danimaldan.redactor.applicator.RedactionApplicatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RedactableAspect {
    @AfterReturning(pointcut = "@annotation(com.danimaldan.redactor.aop.Redact)", returning = "returnValue")
    public Object performAround(Object returnValue) {
        var redactionApplicator = RedactionApplicatorFactory.create(returnValue);

        redactionApplicator.redact();

        return returnValue;
    }
}
