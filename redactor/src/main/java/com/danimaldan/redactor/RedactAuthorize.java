package com.danimaldan.redactor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedactAuthorize {
    /**
     * Required Spring Security authority to view field. Leave empty if only needing to authorize nested props.
     */
    String value() default "";
}
