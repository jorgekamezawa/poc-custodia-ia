package com.bancoexemplo.custody.archfixture.domain;

import java.util.UUID;

/** An entity written as a record, so the architecture test can prove its rule fails. */
public record EntityAsRecord(UUID id, String name) {}
