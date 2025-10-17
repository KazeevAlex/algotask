package com.example.algotask.currency;

public enum RubleNominal implements Nominal {
    RUB_5000(5000),
    RUB_1000(1000),
    RUB_500(500),
    RUB_100(100),
    RUB_50(50),
    DEFAULT(0);

    private final Integer nominal;

    RubleNominal(Integer nominal) {
        this.nominal = nominal;
    }

    @Override
    public Integer getNominal() {
        return nominal;
    }
}
