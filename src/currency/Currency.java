package currency;

public enum Currency {
    EUR(EuroNominal.DEFAULT),
    RUB(RubleNominal.DEFAULT);

    private final Nominal defaultMinValue;

    Currency(Nominal defaultMinValue) {
        this.defaultMinValue = defaultMinValue;
    }

    public Nominal getDefaultMinValue() {
        return defaultMinValue;
    }
}
