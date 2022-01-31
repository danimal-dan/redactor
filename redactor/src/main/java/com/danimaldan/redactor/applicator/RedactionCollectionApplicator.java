package com.danimaldan.redactor.applicator;

import com.danimaldan.redactor.RedactAuthorize;
import com.danimaldan.redactor.Redactable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Objects;

@Slf4j
class RedactionCollectionApplicator<T> implements RedactionApplicator, ReflectionUtils.FieldCallback {
    private static final ReflectionUtils.FieldFilter REDACT_AUTHORIZE_FIELD_FILTER = field -> field.isAnnotationPresent(RedactAuthorize.class);

    private final Collection<T> collection;
    private final HasAuthorityPredicate hasAuthorityPredicate;

    public RedactionCollectionApplicator(Collection<T> collection, HasAuthorityPredicate hasAuthorityPredicate) {
        this.collection = collection;
        this.hasAuthorityPredicate = hasAuthorityPredicate;
    }

    @Override
    public void redact() {
        if (CollectionUtils.isEmpty(collection)) {
            return;
        }

        var firstElement = collection.iterator().next();

        ReflectionUtils.doWithFields(firstElement.getClass(), this, REDACT_AUTHORIZE_FIELD_FILTER);
    }

    @Override
    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        RedactAuthorize redactAuthorize = field.getAnnotation(RedactAuthorize.class);

        String requiredAuthority = redactAuthorize.value();
        if (!hasAuthority(requiredAuthority)) {
            // not authorized to view field, redact it
            Assert.isTrue(Redactable.class.isAssignableFrom(field.getType()), "@RedactAuthorize authorization is denied for field '" + field.toGenericString() + "', but field is not Redactable.");

            this.collection.stream()
                    .map(element -> (Redactable<?>) invokeGetter(field, element))
                    .filter(Objects::nonNull)
                    .forEach(Redactable::redact);
        }

        // run redaction applicator on child properties of the field
        initializeAndRedactFieldValues(field);
    }

    private boolean hasAuthority(@Nullable String requiredAuthority) {
        if (ObjectUtils.isEmpty(requiredAuthority)) {
            return true;
        }

        return hasAuthorityPredicate.hasAuthority(requiredAuthority);
    }

    private void initializeAndRedactFieldValues(Field field) {
        this.collection.stream()
                .map(element -> invokeGetter(field, element))
                .filter(Objects::nonNull)
                .map(fieldValue -> {
                    // optionally unwrap Redactable field value
                    if (Redactable.class.isAssignableFrom(fieldValue.getClass())) {
                        Redactable<?> redactableField = (Redactable<?>) fieldValue;

                        return redactableField.getValue();
                    }

                    return fieldValue;
                })
                .map(fieldValue -> RedactionApplicatorFactory.create(fieldValue, this.hasAuthorityPredicate))
                .forEach(RedactionApplicator::redact);
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
