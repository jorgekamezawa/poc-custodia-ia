package com.bancoexemplo.custody.archfixture.api;

/** A concrete class with logic in a module api, so the architecture test can prove its rules fail. */
public final class LogicInTheApi {

    private LogicInTheApi() {}

    public static int twice(int value) {
        return value * 2;
    }
}
