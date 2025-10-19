package com.example.algotask.atm;

import com.example.algotask.currency.Currency;
import com.example.algotask.currency.Nominal;
import com.example.algotask.message.Message;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReservationATM {

    private static final int ZERO_AMOUNT = 0;

    private final AtmState atmState;

    public ReservationATM(AtmState atmState) {
        this.atmState = atmState;
    }

    public Map<Currency, Map<Nominal, Integer>> withdraw(Currency currency, Integer withdrawalAmount) {
        currency.getLock().lock();
        try {
            return getBanknotes(currency, withdrawalAmount);
        } finally {
            currency.getLock().unlock();
        }
    }

    public void topUp(Map<Currency, Map<Nominal, Integer>> topUpCurrencies) {
        for (var entry : topUpCurrencies.entrySet()) {
            Currency topUpCurrency = entry.getKey();
            Map<Nominal, Integer> topUpBanknotes = entry.getValue();

            topUpCurrency.getLock().lock();
            try {
                atmState.addCurrency(topUpCurrency, topUpBanknotes);
            } finally {
                topUpCurrency.getLock().unlock();
            }
        }
    }

    public UUID reserve(Currency currency, Integer reserveAmount) {
        UUID reservationUuid = UUID.randomUUID();
        Map<Currency, Map<Nominal, Integer>> reservedBanknotes;

        currency.getLock().lock();
        try {
            reservedBanknotes = getBanknotes(currency, reserveAmount);
        } finally {
            currency.getLock().unlock();
        }

        atmState.putReservation(reservationUuid, reservedBanknotes);
        return reservationUuid;
    }

    public Map<Currency, Map<Nominal, Integer>> withdrawReservation(UUID reservationUuid) {
        checkReservationUuid(reservationUuid);
        return atmState.removeReservation(reservationUuid);
    }

    private Map<Currency, Map<Nominal, Integer>> getBanknotes(Currency currency, Integer amount) {
        checkWithdrawalAmount(currency, amount);
        var withdrawalBanknotes = prepareWithdrawalBanknotes(currency, amount);
        atmState.remove(withdrawalBanknotes);
        return withdrawalBanknotes;
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

    private void checkReservationUuid(UUID reservationUuid) {
        if (!atmState.containsReservation(reservationUuid)) {
            String message = MessageFormat.format(Message.RESERVATION_NOT_EXIST.getPattern(), reservationUuid);
            throw new IllegalStateException(message);
        }
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
