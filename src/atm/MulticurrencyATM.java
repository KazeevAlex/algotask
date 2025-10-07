package atm;

import currency.Currency;
import currency.Nominal;
import message.Message;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MulticurrencyATM {

    private static final int DEFAULT_BALANCE_VALUE = 0;
    private static final int ZERO_AMOUNT = 0;

    private final Map<Currency, Map<Nominal, Integer>> banknotes = new HashMap<>();;
    private final Map<Currency,Integer> balances = new HashMap<>();
    private final Map<Currency, Nominal> minNominals = new HashMap<>();

    public MulticurrencyATM(Map<Currency, Map<Nominal, Integer>> banknotes) {
        banknotes.forEach((key, value) -> this.banknotes.put(key, getSortedNominalMap(value)));
        this.banknotes.keySet().forEach(this::updateAtmCurrentState);
    }

    public Map<Currency, Map<Nominal, Integer>> withdraw(Currency currency, Integer withdrawalAmount) {
        checkWithdrawalAmount(currency, withdrawalAmount);
        var withdrawalBanknotes = prepareWithdrawalBanknotes(currency, withdrawalAmount);
        updateAtmCurrentState(currency);
        return withdrawalBanknotes;
    }

    public void topUp(Map<Currency, Map<Nominal, Integer>> topUpCurrencies) {
        for (var entry : topUpCurrencies.entrySet()) {
            Currency topUpCurrency = entry.getKey();
            Map<Nominal, Integer> topUpBanknotes = entry.getValue();

            if (banknotes.containsKey(topUpCurrency)) {
                topUpBanknotes.forEach((nominal, amount) -> banknotes.get(topUpCurrency).merge(nominal, amount, Integer::sum));
            } else {
                banknotes.put(topUpCurrency, getSortedNominalMap(topUpBanknotes));
            }
        }
        topUpCurrencies.keySet().forEach(this::updateAtmCurrentState);
    }

    private Map<Currency, Map<Nominal, Integer>> prepareWithdrawalBanknotes(Currency currency, Integer withdrawalAmount) {
        Map<Nominal, Integer> withdrawalBanknotes = new HashMap<>();
        int remainsAmount = withdrawalAmount;

        for (var entry : banknotes.get(currency).entrySet()) {
            Nominal key = entry.getKey();
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

            if (remainsAmount == ZERO_AMOUNT) {
                break;
            }
        }

        checkRemainsAmount(currency, withdrawalAmount, remainsAmount);

        return Map.of(currency, withdrawalBanknotes);
    }

    private void checkRemainsAmount(Currency currency, Integer withdrawalAmount, Integer remainsAmount) {
        if (remainsAmount == ZERO_AMOUNT) {
            return;
        }
        int nearestMin = withdrawalAmount - remainsAmount;
        int nearestMax = banknotes.get(currency).keySet().stream()
                .mapToInt(Nominal::getNominal)
                .filter(i -> i > withdrawalAmount)
                .min()
                .getAsInt();

        String message = MessageFormat.format(Message.NEAREST_AVAILABLE_AMOUNTS.getPattern(), nearestMin, nearestMax);
        throw new IllegalStateException(message);
    }

    private void checkWithdrawalAmount(Currency currency, Integer withdrawalAmount) {
        if (withdrawalAmount > getBalance(currency)) {
            throw new IllegalStateException(Message.INSUFFICIENT_FUNDS.getPattern());
        }

        int minNominal = getMinNominal(currency).getNominal();
        if (withdrawalAmount % minNominal > 0) {
            String message = MessageFormat.format(Message.AMOUNT_MUST_BE_MULTIPLE_OF.getPattern(), minNominal);
            throw new IllegalStateException(message);
        }
    }

    public Map<Nominal, Integer> getSortedNominalMap(Map<Nominal, Integer> unsortedMap) {
        Map<Nominal, Integer> sortedMap = new TreeMap<>(Nominal.getComparator());
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }

    private void updateAtmCurrentState(Currency currency) {
        removeEndedNominals(currency);
        recalculateBalance(currency);
        updateMinNominal(currency);
    }

    private void removeEndedNominals(Currency currency) {
        banknotes.get(currency).values().removeIf(value -> value < 1);
    }

    private void recalculateBalance(Currency currency) {
        int newBalance = banknotes.get(currency).entrySet().stream()
                .mapToInt(entry -> entry.getKey().getNominal() * entry.getValue())
                .sum();

        balances.put(currency, newBalance);
    }

    private void updateMinNominal(Currency currency) {
        Nominal newMinNominal = banknotes.get(currency).keySet().stream()
                .min(Comparator.comparingInt(Nominal::getNominal))
                .orElse(currency.getDefaultMinValue());

        minNominals.put(currency, newMinNominal);
    }

    public Integer getBalance(Currency currency) {
        return balances.getOrDefault(currency, DEFAULT_BALANCE_VALUE);
    }

    public Nominal getMinNominal(Currency currency) {
        return minNominals.getOrDefault(currency, currency.getDefaultMinValue());
    }
}
