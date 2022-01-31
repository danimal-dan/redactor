package com.danimaldan.redactor.applicator;

@FunctionalInterface
public interface HasAuthorityPredicate {
    /**
     * Determines if the user has access to the authority.
     */
    boolean hasAuthority(String authority);
}
