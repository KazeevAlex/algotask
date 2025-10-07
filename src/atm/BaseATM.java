package atm;

import currency.RubleNominal;
import message.Message;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

public class BaseATM {

    private final Map<RubleNominal, Integer> banknotes;
    private Integer balance;
    private RubleNominal minNominal;

    public BaseATM(Map<RubleNominal, Integer> banknotes) {
        this.banknotes = new EnumMap<>(banknotes);
        updateAtmCurrentState();
    }

    public Integer getBalance() {
        return balance;
    }

    public RubleNominal getMinNominal() {
        return minNominal;
    }

    public Map<RubleNominal, Integer> withdraw(Integer withdrawalAmount) {
        checkWithdrawalAmount(withdrawalAmount);
        var withdrawalBanknotes = prepareWithdrawalBanknotes(withdrawalAmount);
        updateAtmCurrentState();
        return withdrawalBanknotes;
    }

    public void topUp(Map<RubleNominal, Integer> topUpBanknotes) {
        topUpBanknotes.forEach((k, v) -> banknotes.merge(k, v, Integer::sum));
        updateAtmCurrentState();
    }

    private Map<RubleNominal, Integer> prepareWithdrawalBanknotes(Integer withdrawalAmount) {
        Map<RubleNominal, Integer> withdrawalBanknotes = new EnumMap<>(RubleNominal.class);
        int remainsAmount = withdrawalAmount;

        for (var entry : banknotes.entrySet()) {
            RubleNominal key = entry.getKey();
            Integer atmBanknoteAmount = entry.getValue();
            int nominal = key.getNominal();

            if (nominal > remainsAmount) {
                continue;
            }

            int requiredBanknoteAmount = remainsAmount / nominal;
            if (atmBanknoteAmount < requiredBanknoteAmount) {
                requiredBanknoteAmount = atmBanknoteAmount;
            }

            remainsAmount -= nominal * requiredBanknoteAmount;
            withdrawalBanknotes.put(key, requiredBanknoteAmount);

            entry.setValue(atmBanknoteAmount - requiredBanknoteAmount);

            if (remainsAmount == 0) {
                break;
            }
        }

        checkRemainsAmount(withdrawalAmount, remainsAmount);

        return withdrawalBanknotes;
    }

    private void checkRemainsAmount(Integer withdrawalAmount, Integer remainsAmount) {
        if (remainsAmount == 0) {
            return;
        }
        int nearestMin = withdrawalAmount - remainsAmount;
        int nearestMax = banknotes.keySet().stream()
                .mapToInt(RubleNominal::getNominal)
                .filter(i -> i > withdrawalAmount)
                .min()
                .getAsInt();

        String message = MessageFormat.format(Message.NEAREST_AVAILABLE_AMOUNTS.getPattern(), nearestMin, nearestMax);
        throw new IllegalStateException(message);
    }

    private void checkWithdrawalAmount(Integer withdrawalAmount) {
        if (withdrawalAmount > balance) {
            throw new IllegalStateException(Message.INSUFFICIENT_FUNDS.getPattern());
        }
        if (withdrawalAmount % minNominal.getNominal() > 0) {
            String message = MessageFormat.format(Message.AMOUNT_MUST_BE_MULTIPLE_OF.getPattern(), minNominal.getNominal());
            throw new IllegalStateException(message);
        }
    }

    private void updateAtmCurrentState() {
        removeEndedNominals();
        updateMinNominal();
        recalculateBalance();
    }

    private void removeEndedNominals() {
        this.banknotes.values().removeIf(value -> value < 1);
    }

    private void updateMinNominal() {
        this.minNominal = banknotes.keySet().stream()
                .min(Comparator.comparingInt(RubleNominal::getNominal))
                .orElse(RubleNominal.DEFAULT);
    }

    private void recalculateBalance() {
        this.balance = banknotes.entrySet().stream()
                .mapToInt(entry -> entry.getKey().getNominal() * entry.getValue())
                .sum();
    }
}
