package com.example.algotask.atm;

import com.example.algotask.currency.Currency;
import com.example.algotask.currency.Nominal;
import com.example.algotask.message.Message;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class MulticurrencyATM {

    private static final int ZERO_AMOUNT = 0;

    private final AtmState atmState;

    public MulticurrencyATM(AtmState atmState) {
        this.atmState = atmState;
    }

    public Map<Currency, Map<Nominal, Integer>> withdraw(Currency currency, Integer withdrawalAmount) {
        checkWithdrawalAmount(currency, withdrawalAmount);
        var withdrawalBanknotes = prepareWithdrawalBanknotes(currency, withdrawalAmount);
        atmState.remove(withdrawalBanknotes);
        return withdrawalBanknotes;
    }

    public void topUp(Map<Currency, Map<Nominal, Integer>> topUpCurrencies) {
        atmState.put(topUpCurrencies);
    }

    private Map<Currency, Map<Nominal, Integer>> prepareWithdrawalBanknotes(Currency currency, Integer withdrawalAmount) {
        Map<Nominal, Integer> withdrawalBanknotes = new HashMap<>();
        int remainsAmount = withdrawalAmount;

        for (var entry : atmState.getBanknotes(currency).entrySet()) {
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
        int nearestMax = atmState.getBanknotes(currency).keySet().stream()
                .mapToInt(Nominal::getNominal)
                .filter(i -> i > withdrawalAmount)
                .min()
                .getAsInt();

        String message = MessageFormat.format(Message.NEAREST_AVAILABLE_AMOUNTS.getPattern(), nearestMin, nearestMax);
        throw new IllegalStateException(message);
    }

    private void checkWithdrawalAmount(Currency currency, Integer withdrawalAmount) {
        if (withdrawalAmount > atmState.getBalance(currency)) {
            throw new IllegalStateException(Message.INSUFFICIENT_FUNDS.getPattern());
        }

        int minNominal = atmState.getMinNominal(currency).getNominal();
        if (withdrawalAmount % minNominal > 0) {
            String message = MessageFormat.format(Message.AMOUNT_MUST_BE_MULTIPLE_OF.getPattern(), minNominal);
            throw new IllegalStateException(message);
        }
    }
}
