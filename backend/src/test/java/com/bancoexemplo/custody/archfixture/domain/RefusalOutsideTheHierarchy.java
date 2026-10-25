package com.bancoexemplo.custody.archfixture.domain;

/** A domain refusal outside an exception package and the domain hierarchy, so the architecture test can prove its rule fails. */
public class RefusalOutsideTheHierarchy extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RefusalOutsideTheHierarchy(String message) {
        super(message);
    }
}
