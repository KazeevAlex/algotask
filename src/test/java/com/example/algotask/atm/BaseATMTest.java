package com.example.algotask.atm;

import com.example.algotask.currency.Nominal;
import com.example.algotask.currency.RubleNominal;
import com.example.algotask.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseATMTest {

    @Mock
    private BaseAtmState atmState;

    @InjectMocks
    private BaseATM atm;

    @Test
    void testSuccessfulWithdraw() {
        int withdrawalAmount = 1600;
        Map<Nominal, Integer> map = Map.of(
                RubleNominal.RUB_5000, 1,
                RubleNominal.RUB_1000, 2,
                RubleNominal.RUB_500, 5,
                RubleNominal.RUB_100, 10
        );
        when(atmState.getBanknotes()).thenReturn(new TreeMap<>(map));
        when(atmState.getBalance()).thenReturn(10500);
        when(atmState.getMinNominal()).thenReturn(RubleNominal.RUB_100);

        Map<Nominal, Integer> withdrawn = atm.withdraw(withdrawalAmount);

        Map<Nominal, Integer> expectedWithdrawal = new HashMap<>();
        expectedWithdrawal.put(RubleNominal.RUB_1000, 1);
        expectedWithdrawal.put(RubleNominal.RUB_500, 1);
        expectedWithdrawal.put(RubleNominal.RUB_100, 1);

        assertEquals(expectedWithdrawal, withdrawn);
        verify(atmState, times(1)).remove(expectedWithdrawal);
    }

    @Test
    void testWithdrawInsufficientFunds() {
        when(atmState.getBalance()).thenReturn(1000);
        Exception exception = assertThrows(IllegalStateException.class, () -> atm.withdraw(2000));
        assertEquals(Message.INSUFFICIENT_FUNDS.getPattern(), exception.getMessage());
    }

    @Test
    void testWithdrawAmountNotMultipleOfMinNominal() {
        when(atmState.getBalance()).thenReturn(5000);
        when(atmState.getMinNominal()).thenReturn(RubleNominal.RUB_100);

        Exception exception = assertThrows(IllegalStateException.class, () -> atm.withdraw(150));

        String expectedMessage = MessageFormat.format(Message.AMOUNT_MUST_BE_MULTIPLE_OF.getPattern(), 100);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testTopUp() {
        Map<Nominal, Integer> toAdd = new HashMap<>();
        toAdd.put(RubleNominal.RUB_100, 5);
        toAdd.put(RubleNominal.RUB_50, 20);

        atm.topUp(toAdd);

        verify(atmState, times(1)).put(toAdd);
    }

    @Test
    void testWithdrawCannotGiveExactAmount() {
        Map<Nominal, Integer> notes = new TreeMap<>(Nominal.getComparator());
        notes.put(RubleNominal.RUB_500, 1);
        notes.put(RubleNominal.RUB_100, 3);

        when(atmState.getBalance()).thenReturn(800);
        when(atmState.getMinNominal()).thenReturn(RubleNominal.RUB_100);
        when(atmState.getBanknotes()).thenReturn(notes);

        Exception exception = assertThrows(IllegalStateException.class, () -> atm.withdraw(400));
        String message = MessageFormat.format(Message.NEAREST_AVAILABLE_AMOUNTS.getPattern(), 300, 500);
        assertTrue(exception.getMessage().contains(message));
    }
}
