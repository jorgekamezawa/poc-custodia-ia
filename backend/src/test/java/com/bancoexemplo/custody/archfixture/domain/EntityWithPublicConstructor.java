package com.bancoexemplo.custody.archfixture.domain;

/** An entity anyone can build without its factory, so the architecture test can prove its rule fails. */
public final class EntityWithPublicConstructor {

    private final String name;

    public EntityWithPublicConstructor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
