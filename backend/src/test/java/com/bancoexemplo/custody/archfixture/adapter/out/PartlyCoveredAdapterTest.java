package com.bancoexemplo.custody.archfixture.adapter.out;

/** Calls only one of the adapter's methods; it has no test of its own to run. */
final class PartlyCoveredAdapterTest {

    private PartlyCoveredAdapterTest() {}

    static int callsOnlyTheCoveredMethod() {
        return new PartlyCoveredAdapter().covered();
    }
}
