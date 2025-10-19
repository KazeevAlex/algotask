package com.example.algotask.atm;

import com.example.algotask.currency.Currency;
import com.example.algotask.currency.Nominal;
import com.example.algotask.currency.NominalUtils;
import com.example.algotask.message.Message;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AtmState {

    private final Map<Currency, Map<Nominal, Integer>> banknotes = new HashMap<>();
    private final Map<UUID, Map<Currency, Map<Nominal, Integer>>> reservation = new ConcurrentHashMap<>();

    public AtmState(Map<Currency, Map<Nominal, Integer>> banknotes) {
        this.put(banknotes);
    }

    public void putReservation(UUID reservationUuid, Map<Currency, Map<Nominal, Integer>> reservedBanknotes) {
        Map<Currency, Map<Nominal, Integer>> copy = new HashMap<>();
        reservedBanknotes.forEach((currency, banknotes) -> copy.put(currency, Map.copyOf(banknotes)));
        reservation.put(reservationUuid, copy);
    }

    public boolean containsReservation(UUID reservationUuid) {
        return reservation.containsKey(reservationUuid);
    }

    public Map<Currency, Map<Nominal, Integer>> removeReservation(UUID reservationUuid) {
        return reservation.remove(reservationUuid);
    }

    public void put(Map<Currency, Map<Nominal, Integer>> newBanknotes) {
        if (newBanknotes == null || newBanknotes.isEmpty()) {
            return;
        }
        newBanknotes.forEach(this::addCurrency);
    }

    public void addCurrency(Currency currency, Map<Nominal, Integer> newBanknotes) {
        Objects.requireNonNull(currency);
        if (newBanknotes == null || newBanknotes.isEmpty()) {
            return;
        }
        final var atmBanknotes = banknotes.computeIfAbsent(currency, k -> NominalUtils.getSortedNominalMap());
        newBanknotes.forEach((nominal, amount) -> addBanknotes(atmBanknotes, nominal, amount));
    }

    private void addBanknotes(Map<Nominal, Integer> atmBanknotes, Nominal nominal, Integer amount) {
        if (amount == null || amount < 1) {
            return;
        }
        atmBanknotes.merge(nominal, amount, Integer::sum);
    }

    public void remove(Map<Currency, Map<Nominal, Integer>> removedBanknotes) {
        if (removedBanknotes == null || removedBanknotes.isEmpty()) {
            return;
        }
        removedBanknotes.forEach(this::removeCurrency);
    }

    private void removeCurrency(Currency currency, Map<Nominal, Integer> removedBanknotes) {
        if (currency == null || removedBanknotes == null || removedBanknotes.isEmpty()) {
            return;
        }
        final var atmBanknotes = banknotes.get(currency);
        removedBanknotes.forEach((nominal, amount) -> removeBanknote(atmBanknotes, nominal, amount));
    }

    private void removeBanknote(Map<Nominal, Integer> atmBanknotes, Nominal nominal, Integer amount) {
        if (amount == null || amount < 1) {
            return;
        }
        Integer atmNominalAmount = atmBanknotes.getOrDefault(nominal, 0);
        if (atmNominalAmount < amount) {
            throw new IllegalStateException(Message.INSUFFICIENT_FUNDS.getPattern());
        }
        if (atmNominalAmount - amount == 0) {
            atmBanknotes.remove(nominal);
            return;
        }
        atmBanknotes.merge(nominal, amount, (atm, withdrawal) -> atm - withdrawal);
    }

    public Map<Nominal, Integer> getBanknotes(Currency currency) {
        return Collections.unmodifiableMap(banknotes.computeIfAbsent(currency, k -> Collections.emptyMap()));
    }

    public Integer getBalance(Currency currency) {
        return banknotes.computeIfAbsent(currency, k -> Collections.emptyMap())
                .entrySet().stream()
                .mapToInt(entry -> entry.getKey().getNominal() * entry.getValue())
                .sum();
    }

    public Nominal getMinNominal(Currency currency) {
        Objects.requireNonNull(currency);
        return banknotes.computeIfAbsent(currency, k -> Collections.emptyMap())
                .keySet().stream()
                .min(Comparator.comparingInt(Nominal::getNominal))
                .orElse(currency.getDefaultMinValue());
    }
}
