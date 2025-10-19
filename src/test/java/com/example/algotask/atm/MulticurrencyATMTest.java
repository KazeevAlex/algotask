package com.example.algotask.atm;

import com.example.algotask.currency.Currency;
import com.example.algotask.currency.Nominal;
import com.example.algotask.currency.RubleNominal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MulticurrencyATMTest {

    @Mock
    private AtmState atmState;

    private MulticurrencyATM multicurrencyATM;

    @BeforeEach
    void setUp() {
        multicurrencyATM = new MulticurrencyATM(atmState);
    }

    @Test
    void withdraw_shouldReturnCorrectBanknotes_whenSufficientFunds() {
        // given
        when(atmState.getBanknotes(Currency.RUB)).thenReturn(Map.of(
                RubleNominal.RUB_1000, 5,
                RubleNominal.RUB_500, 10,
                RubleNominal.RUB_100, 10,
                RubleNominal.RUB_50, 10
        ));
        when(atmState.getBalance(Currency.RUB)).thenReturn(11500);
        when(atmState.getMinNominal(Currency.RUB)).thenReturn(RubleNominal.RUB_50);

        // when
        Map<Currency, Map<Nominal, Integer>> result = multicurrencyATM.withdraw(Currency.RUB, 3850);

        // then
        Map<Nominal, Integer> expected = Map.of(
                RubleNominal.RUB_1000, 3,
                RubleNominal.RUB_500, 1,
                RubleNominal.RUB_100, 3,
                RubleNominal.RUB_50, 1
        );
        assertEquals(expected, result.get(Currency.RUB));
    }

    @Test
    void withdraw_shouldThrowException_whenInsufficientFunds() {
        // given
        when(atmState.getBalance(Currency.RUB)).thenReturn(500);

        // when & then
        assertThrows(IllegalStateException.class, () -> multicurrencyATM.withdraw(Currency.RUB, 600));
    }

    @Test
    void withdraw_shouldThrowException_whenAmountNotMultipleOfMinNominal() {
        // given
        when(atmState.getBalance(Currency.RUB)).thenReturn(1000);
        when(atmState.getMinNominal(Currency.RUB)).thenReturn(RubleNominal.RUB_50);

        // when & then
        assertThrows(IllegalStateException.class, () -> multicurrencyATM.withdraw(Currency.RUB, 125));
    }

    @Test
    void topUp_shouldCallAtmStatePut() {
        // given
        Map<Currency, Map<Nominal, Integer>> topUpCurrencies = Map.of(
                Currency.RUB, Map.of(RubleNominal.RUB_100, 5)
        );

        // when
        multicurrencyATM.topUp(topUpCurrencies);

        // then
        verify(atmState).put(topUpCurrencies);
    }
}