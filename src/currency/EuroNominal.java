package currency;

public enum EuroNominal implements Nominal {
    EUR_500(500),
    EUR_100(100),
    EUR_20(20),
    DEFAULT(0);

    private final Integer nominal;

    EuroNominal(Integer nominal) {
        this.nominal = nominal;
    }

    @Override
    public Integer getNominal() {
        return nominal;
    }
}
