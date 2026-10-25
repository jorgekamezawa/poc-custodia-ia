package com.bancoexemplo.custody.archfixture.adapter.out;

/** An outbound adapter with no test class, so the architecture test can prove its rule fails. */
public final class UntestedAdapter {

    public int anything() {
        return 3;
    }
}
