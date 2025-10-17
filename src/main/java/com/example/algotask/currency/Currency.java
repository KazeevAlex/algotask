package com.example.algotask.currency;

import java.util.concurrent.locks.ReentrantLock;

public enum Currency {
    EUR(EuroNominal.DEFAULT, new ReentrantLock(true)),
    RUB(RubleNominal.DEFAULT, new ReentrantLock(true));

    private final Nominal defaultMinValue;
    private final ReentrantLock lock;

    Currency(Nominal defaultMinValue, ReentrantLock lock) {
        this.defaultMinValue = defaultMinValue;
        this.lock = lock;
    }

    public Nominal getDefaultMinValue() {
        return defaultMinValue;
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
