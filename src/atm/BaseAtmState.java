package atm;

import currency.Nominal;
import currency.NominalUtils;
import currency.RubleNominal;

import java.util.Comparator;
import java.util.Map;

public class BaseAtmState {

    private final Map<Nominal, Integer> banknotes;
    private Integer balance;
    private Nominal minNominal;

    public BaseAtmState(Map<Nominal, Integer> banknotes) {
        this.banknotes = NominalUtils.getSortedNominalMap(banknotes);
        updateAtmCurrentState();
    }

    public Map<Nominal, Integer> getBanknotes() {
        return banknotes;
    }

    public Integer getBalance() {
        return balance;
    }

    public Nominal getMinNominal() {
        return minNominal;
    }

    public void updateAtmCurrentState() {
        removeEndedNominals();
        updateMinNominal();
        recalculateBalance();
    }

    private void removeEndedNominals() {
        this.banknotes.values().removeIf(value -> value < 1);
    }

    private void updateMinNominal() {
        this.minNominal = banknotes.keySet().stream()
                .min(Comparator.comparingInt(Nominal::getNominal))
                .orElse(RubleNominal.DEFAULT);
    }

    private void recalculateBalance() {
        this.balance = banknotes.entrySet().stream()
                .mapToInt(entry -> entry.getKey().getNominal() * entry.getValue())
                .sum();
    }
}
