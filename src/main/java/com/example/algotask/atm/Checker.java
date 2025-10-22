package com.example.algotask.atm;

import com.example.algotask.currency.Currency;
import com.example.algotask.currency.Nominal;
import com.example.algotask.message.Message;

import java.text.MessageFormat;
import java.util.Map;
import java.util.UUID;

public class Checker {

    private static final int ZERO_AMOUNT = 0;
    private static final double MAX_RESERVATION_PERCENTAGE = 5;

    private final AtmState atmState;

    public Checker(AtmState atmState) {
        this.atmState = atmState;
    }

    public void checkMaxReservationAmount(Currency currency, Integer reserveAmount) {
        Integer balance = atmState.getBalance(currency);
        if (reserveAmount > balance) {
            throw new IllegalStateException(Message.INSUFFICIENT_FUNDS.getPattern());
        }
        int maxReservationAmount = (int) (balance * (MAX_RESERVATION_PERCENTAGE / 100));
        maxReservationAmount -= maxReservationAmount % atmState.getMinNominal(currency).getNominal();
        if (reserveAmount > maxReservationAmount) {
            String message;
            if (maxReservationAmount == 0) {
                message = Message.INSUFFICIENT_FUNDS.getPattern();
            } else {
                message = MessageFormat.format(Message.EXCEEDING_MAXIMUM_RESERVATION_AMOUNT.getPattern(), maxReservationAmount);
            }
            throw new IllegalStateException(message);
        }
    }

    public void checkReservation(UUID reservationUuid, Map<Currency, Map<Nominal, Integer>> reservation) {
        if (reservation == null || reservation.isEmpty()) {
            String message = MessageFormat.format(Message.RESERVATION_NOT_EXIST.getPattern(), reservationUuid);
            throw new IllegalStateException(message);
        }
    }

    public void checkRemainsAmount(Currency currency, Integer withdrawalAmount, Integer remainsAmount) {
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

    public void checkWithdrawalAmount(Currency currency, Integer withdrawalAmount) {
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
