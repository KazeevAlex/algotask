package atm;

import currency.Nominal;
import currency.NominalUtils;
import currency.RubleNominal;
import message.Message;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class BaseAtmState {

    private final Map<Nominal, Integer> banknotes;

    public BaseAtmState(Map<Nominal, Integer> banknotes) {
        this.banknotes = NominalUtils.getSortedNominalMap();
        this.put(banknotes);
    }

    public void put(Map<Nominal, Integer> banknotes) {
        if (banknotes == null || banknotes.isEmpty()) {
            return;
        }
        banknotes.forEach(this::addBanknote);
    }

    private void addBanknote(Nominal nominal, Integer amount) {
        if (nominal == null || amount == null || amount < 1) {
            return;
        }
        banknotes.merge(nominal, amount, Integer::sum);
    }

    public void remove(Map<Nominal, Integer> banknotes) {
        if (banknotes == null || banknotes.isEmpty()) {
            return;
        }
        banknotes.forEach(this::removeBanknote);
    }

    private void removeBanknote(Nominal nominal, Integer amount) {
        if (amount == null || amount < 1) {
            return;
        }
        Integer atmNominalAmount = banknotes.getOrDefault(nominal, 0);
        if (atmNominalAmount < amount) {
            throw new IllegalStateException(Message.INSUFFICIENT_FUNDS.getPattern());
        }
        if (atmNominalAmount - amount == 0) {
            banknotes.remove(nominal);
            return;
        }
        banknotes.merge(nominal, amount, (atm, withdrawal) -> atm - withdrawal);
    }

    public Map<Nominal, Integer> getBanknotes() {
        return Collections.unmodifiableMap(banknotes);
    }

    public Integer getBalance() {
        return banknotes.entrySet().stream()
                .mapToInt(entry -> entry.getKey().getNominal() * entry.getValue())
                .sum();
    }

    public Nominal getMinNominal() {
        return banknotes.keySet().stream()
                .min(Comparator.comparingInt(Nominal::getNominal))
                .orElse(RubleNominal.DEFAULT);
    }
}
