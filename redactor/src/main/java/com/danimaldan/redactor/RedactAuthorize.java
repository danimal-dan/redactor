package com.danimaldan.redactor;

import org.springframework.core.annotation.AliasFor;

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

    // TODO: Implement Spring annotation utils so this gets populated
    @AliasFor("value")
    String readAuthority() default "";

    /**
     * Defaults to {@link #readAuthority()}, but can specify a separate authority that is required to update the field.
     */
    String updateAuthority() default "";
}
