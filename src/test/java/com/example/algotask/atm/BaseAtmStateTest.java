package com.example.algotask.atm;

import com.example.algotask.currency.Nominal;
import com.example.algotask.currency.RubleNominal;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BaseAtmStateTest {

    @Test
    void testPut() {
        BaseAtmState atmState = new BaseAtmState(new HashMap<>());
        Map<Nominal, Integer> banknotesToPut = new HashMap<>();
        banknotesToPut.put(RubleNominal.RUB_50, 10);
        banknotesToPut.put(RubleNominal.RUB_100, 5);

        atmState.put(banknotesToPut);

        assertEquals(10, atmState.getBanknotes().get(RubleNominal.RUB_50));
        assertEquals(5, atmState.getBanknotes().get(RubleNominal.RUB_100));
    }

    @Test
    void testPutNullOrEmpty() {
        BaseAtmState atmState = new BaseAtmState(new HashMap<>());
        atmState.put(null);
        assertTrue(atmState.getBanknotes().isEmpty());

        atmState.put(new HashMap<>());
        assertTrue(atmState.getBanknotes().isEmpty());
    }

    @Test
    void testRemove() {
        Map<Nominal, Integer> initialBanknotes = new HashMap<>();
        initialBanknotes.put(RubleNominal.RUB_50, 10);
        initialBanknotes.put(RubleNominal.RUB_100, 5);
        BaseAtmState atmState = new BaseAtmState(initialBanknotes);

        Map<Nominal, Integer> banknotesToRemove = new HashMap<>();
        banknotesToRemove.put(RubleNominal.RUB_50, 3);
        banknotesToRemove.put(RubleNominal.RUB_100, 2);

        atmState.remove(banknotesToRemove);

        assertEquals(7, atmState.getBanknotes().get(RubleNominal.RUB_50));
        assertEquals(3, atmState.getBanknotes().get(RubleNominal.RUB_100));
    }

    @Test
    void testRemoveInsufficientFunds() {
        Map<Nominal, Integer> initialBanknotes = new HashMap<>();
        initialBanknotes.put(RubleNominal.RUB_50, 10);
        BaseAtmState atmState = new BaseAtmState(initialBanknotes);

        Map<Nominal, Integer> banknotesToRemove = new HashMap<>();
        banknotesToRemove.put(RubleNominal.RUB_50, 11);

        assertThrows(IllegalStateException.class, () -> atmState.remove(banknotesToRemove));
    }

    @Test
    void testGetBalance() {
        Map<Nominal, Integer> initialBanknotes = new HashMap<>();
        initialBanknotes.put(RubleNominal.RUB_50, 10); // 500
        initialBanknotes.put(RubleNominal.RUB_100, 5); // 500
        initialBanknotes.put(RubleNominal.RUB_5000, 1); // 5000
        BaseAtmState atmState = new BaseAtmState(initialBanknotes);

        assertEquals(6000, atmState.getBalance());
    }

    @Test
    void testGetMinNominal() {
        Map<Nominal, Integer> initialBanknotes = new HashMap<>();
        initialBanknotes.put(RubleNominal.RUB_100, 5);
        initialBanknotes.put(RubleNominal.RUB_50, 10);
        BaseAtmState atmState = new BaseAtmState(initialBanknotes);

        assertEquals(RubleNominal.RUB_50, atmState.getMinNominal());
    }

    @Test
    void testGetMinNominalEmpty() {
        BaseAtmState atmState = new BaseAtmState(new HashMap<>());
        assertEquals(RubleNominal.DEFAULT, atmState.getMinNominal());
    }
}
