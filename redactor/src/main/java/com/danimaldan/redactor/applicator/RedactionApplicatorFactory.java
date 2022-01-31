package com.danimaldan.redactor.applicator;

import lombok.experimental.UtilityClass;

import java.util.Collection;

@UtilityClass
public class RedactionApplicatorFactory {
    private static final HasAuthorityPredicate DEFAULT_HAS_AUTHORITY_PREDICATE = authority -> false;

    public RedactionApplicator create(Object object) {
        return create(object, DEFAULT_HAS_AUTHORITY_PREDICATE);
    }

    public RedactionApplicator create(Object object, HasAuthorityPredicate hasAuthorityPredicate) {
        if (object == null) {
            return new RedactionNullApplicator();
        }

        if (Collection.class.isAssignableFrom(object.getClass())) {
            return new RedactionCollectionApplicator<>((Collection<?>) object, hasAuthorityPredicate);
        }

        return new RedactionObjectApplicator(object, hasAuthorityPredicate);
    }
}
