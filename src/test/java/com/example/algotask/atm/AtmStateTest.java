package com.example.algotask.atm;

import com.example.algotask.currency.Currency;
import com.example.algotask.currency.Nominal;
import com.example.algotask.currency.RubleNominal;
import com.example.algotask.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AtmStateTest {

    private AtmState atmState;

    @BeforeEach
    void setUp() {
        Map<Currency, Map<Nominal, Integer>> initialBanknotes = new HashMap<>();
        Map<Nominal, Integer> rubBanknotes = new HashMap<>();
        rubBanknotes.put(RubleNominal.RUB_50, 10);
        rubBanknotes.put(RubleNominal.RUB_100, 10);
        initialBanknotes.put(Currency.RUB, rubBanknotes);
        atmState = new AtmState(initialBanknotes);
    }

    @Test
    void testPutBanknotes() {
        Map<Currency, Map<Nominal, Integer>> newBanknotes = new HashMap<>();
        Map<Nominal, Integer> rubBanknotes = new HashMap<>();
        rubBanknotes.put(RubleNominal.RUB_50, 5);
        rubBanknotes.put(RubleNominal.RUB_500, 5);
        newBanknotes.put(Currency.RUB, rubBanknotes);
        atmState.put(newBanknotes);

        assertEquals(15, atmState.getBanknotes(Currency.RUB).get(RubleNominal.RUB_50));
        assertEquals(5, atmState.getBanknotes(Currency.RUB).get(RubleNominal.RUB_500));
    }

    @Test
    void testRemoveBanknotes() {
        Map<Currency, Map<Nominal, Integer>> removedBanknotes = new HashMap<>();
        Map<Nominal, Integer> rubBanknotes = new HashMap<>();
        rubBanknotes.put(RubleNominal.RUB_50, 5);
        removedBanknotes.put(Currency.RUB, rubBanknotes);
        atmState.remove(removedBanknotes);

        assertEquals(5, atmState.getBanknotes(Currency.RUB).get(RubleNominal.RUB_50));
    }

    @Test
    void testRemoveMoreBanknotesThanAvailable() {
        Map<Currency, Map<Nominal, Integer>> removedBanknotes = new HashMap<>();
        Map<Nominal, Integer> rubBanknotes = new HashMap<>();
        rubBanknotes.put(RubleNominal.RUB_50, 15);
        removedBanknotes.put(Currency.RUB, rubBanknotes);

        Exception exception = assertThrows(IllegalStateException.class, () -> atmState.remove(removedBanknotes));

        String expectedMessage = Message.INSUFFICIENT_FUNDS.getPattern();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testGetBalance() {
        assertEquals(1500, atmState.getBalance(Currency.RUB));
    }

    @Test
    void testGetMinNominal() {
        assertEquals(RubleNominal.RUB_50, atmState.getMinNominal(Currency.RUB));
    }

    @Test
    void testGetMinNominalForNewCurrency() {
        assertEquals(Currency.EUR.getDefaultMinValue(), atmState.getMinNominal(Currency.EUR));
    }

    @Test
    void testPutReservation() {
        UUID reservationUuid = UUID.randomUUID();
        Map<Currency, Map<Nominal, Integer>> reservedBanknotes = Map.of(Currency.RUB, Map.of(RubleNominal.RUB_100, 1));

        atmState.putReservation(reservationUuid, reservedBanknotes);

        assertTrue(atmState.containsReservation(reservationUuid));
        assertEquals(reservedBanknotes, atmState.removeReservation(reservationUuid));
    }

    @Test
    void testContainsReservation_forNonExisting() {
        assertFalse(atmState.containsReservation(UUID.randomUUID()));
    }

    @Test
    void testRemoveReservation() {
        UUID reservationUuid = UUID.randomUUID();
        Map<Currency, Map<Nominal, Integer>> reservedBanknotes = Map.of(Currency.RUB, Map.of(RubleNominal.RUB_100, 1));
        atmState.putReservation(reservationUuid, reservedBanknotes);

        Map<Currency, Map<Nominal, Integer>> removed = atmState.removeReservation(reservationUuid);

        assertEquals(reservedBanknotes, removed);
        assertFalse(atmState.containsReservation(reservationUuid));
        assertNull(atmState.removeReservation(reservationUuid));
    }
}