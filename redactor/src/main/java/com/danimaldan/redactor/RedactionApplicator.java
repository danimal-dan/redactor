package com.danimaldan.redactor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public class RedactionApplicator implements ReflectionUtils.FieldCallback {
    private static final ReflectionUtils.FieldFilter REDACT_AUTHORIZE_FIELD_FILTER = field -> field.isAnnotationPresent(RedactAuthorize.class);
    private static final HasAuthorityPredicate DEFAULT_HAS_AUTHORITY_PREDICATE = authority -> false;

    private final Object object;
    private final HasAuthorityPredicate hasAuthorityPredicate;

    public RedactionApplicator(Object object) {
        this(object, DEFAULT_HAS_AUTHORITY_PREDICATE);
    }

    public RedactionApplicator(Object object, HasAuthorityPredicate hasAuthorityPredicate) {
        this.object = object;
        this.hasAuthorityPredicate = hasAuthorityPredicate;
    }

    @FunctionalInterface
    interface HasAuthorityPredicate {
        /**
         * Determines if the user has access to the authority.
         */
        boolean hasAuthority(String authority);
    }

    public void redact() {
        if (this.object == null) {
            return;
        }

        ReflectionUtils.doWithFields(this.object.getClass(), this, REDACT_AUTHORIZE_FIELD_FILTER);
    }

    @Override
    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        RedactAuthorize redactAuthorize = field.getAnnotation(RedactAuthorize.class);

        String requiredAuthority = redactAuthorize.value();
        if (!hasAuthority(requiredAuthority)) {
            // not authorized to view field, redact it
            Assert.isTrue(Redactable.class.isAssignableFrom(field.getType()), "@RedactAuthorize authorization is denied for field '" + field.toGenericString() + "', but field is not Redactable.");

            Redactable<?> redactableObject = (Redactable<?>) invokeGetter(field, this.object);

            if (redactableObject != null) {
                redactableObject.redact();
            }

            return;
        }

        // run redaction applicator on child properties of the field
        initializeRedactionApplicatorForValueOfField(field)
                .redact();
    }

    private boolean hasAuthority(@Nullable String requiredAuthority) {
        if (ObjectUtils.isEmpty(requiredAuthority)) {
            return true;
        }

        return hasAuthorityPredicate.hasAuthority(requiredAuthority);
    }

    private RedactionApplicator initializeRedactionApplicatorForValueOfField(Field field) {
        Object fieldValue = invokeGetter(field, this.object);

        // unwrap Redactable object if necessary
        if (fieldValue != null && Redactable.class.isAssignableFrom(fieldValue.getClass())) {
            Redactable<?> redactableField = (Redactable<?>) fieldValue;

            fieldValue = redactableField.getValue();
        }

        return new RedactionApplicator(fieldValue, hasAuthorityPredicate);
    }

    private Object invokeGetter(Field field, Object object) {
        if (object == null) {
            return null;
        }

        try {
            var fieldName = field.getName();
            var readMethodName = "is" + StringUtils.capitalize(fieldName); // will also search for 'get' if 'is' prefix fails

            return new PropertyDescriptor(fieldName, object.getClass(), readMethodName, null)
                    .getReadMethod()
                    .invoke(object);
        } catch (InvocationTargetException | IllegalArgumentException | IntrospectionException | IllegalAccessException e) {
            throw new RuntimeException("Could not access getter for field '" + field.getGenericType() + "', a public getter is required for all properties having @RedactAuthorize annotation. Details: " + e.getMessage(), e);
        }
    }
}
