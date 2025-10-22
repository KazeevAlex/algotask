package com.example.algotask.atm;

import com.example.algotask.currency.Currency;
import com.example.algotask.currency.Nominal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReservationATM {

    private static final int ZERO_AMOUNT = 0;

    private final AtmState atmState;
    private final ExpiredReservationScheduler reservationScheduler;
    private final Checker checker;

    public ReservationATM(AtmState atmState) {
        this.atmState = atmState;
        this.reservationScheduler = new ExpiredReservationScheduler(atmState);
        this.checker = new Checker(atmState);
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
            checker.checkMaxReservationAmount(currency, reserveAmount);
            reservedBanknotes = getBanknotes(currency, reserveAmount);
        } finally {
            currency.getLock().unlock();
        }

        atmState.putReservation(reservationUuid, reservedBanknotes);
        reservationScheduler.schedule(reservationUuid, currency);

        return reservationUuid;
    }

    public Map<Currency, Map<Nominal, Integer>> withdrawReservation(UUID reservationUuid) {
        reservationScheduler.cancel(reservationUuid);
        var reservation = atmState.removeReservation(reservationUuid);
        checker.checkReservation(reservationUuid, reservation);
        return reservation;
    }

    private Map<Currency, Map<Nominal, Integer>> getBanknotes(Currency currency, Integer amount) {
        checker.checkWithdrawalAmount(currency, amount);
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

        checker.checkRemainsAmount(currency, withdrawalAmount, remainsAmount);

        return Map.of(currency, withdrawalBanknotes);
    }
}
