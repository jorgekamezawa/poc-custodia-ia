package com.bancoexemplo.custody.archfixture.adapter.out;

/** An outbound adapter whose test leaves a method out, so the architecture test can prove its rule fails. */
public final class PartlyCoveredAdapter {

    public int covered() {
        return 1;
    }

    public int untested() {
        return 2;
    }
}
