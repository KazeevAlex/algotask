package com.example.algotask.atm;

import com.example.algotask.currency.Currency;
import com.example.algotask.currency.Nominal;
import com.example.algotask.currency.RubleNominal;
import com.example.algotask.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationATMTest {

    private AtmState atmState;
    private ReservationATM reservationATM;

    @BeforeEach
    void setUp() {
        Map<Currency, Map<Nominal, Integer>> initialBanknotes = new HashMap<>();
        Map<Nominal, Integer> rubBanknotes = new HashMap<>();
        rubBanknotes.put(RubleNominal.RUB_100, 1000);
        initialBanknotes.put(Currency.RUB, rubBanknotes);
        atmState = new AtmState(initialBanknotes);
        reservationATM = new ReservationATM(atmState);
    }

    @Test
    void testConcurrentWithdraw() throws InterruptedException {
        int threads = 10;
        int withdrawalsPerThread = 10;
        int withdrawalAmount = 100;
        int initialBalance = atmState.getBalance(Currency.RUB);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < withdrawalsPerThread; j++) {
                    reservationATM.withdraw(Currency.RUB, withdrawalAmount);
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        int expectedBalance = initialBalance - (threads * withdrawalsPerThread * withdrawalAmount);
        assertEquals(expectedBalance, atmState.getBalance(Currency.RUB));
    }

    @Test
    void testConcurrentTopUp() throws InterruptedException {
        int threads = 10;
        int topUpsPerThread = 10;
        int topUpAmount = 100;
        int initialBalance = atmState.getBalance(Currency.RUB);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        Map<Currency, Map<Nominal, Integer>> topUp = Map.of(Currency.RUB, Map.of(RubleNominal.RUB_100, 1));

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < topUpsPerThread; j++) {
                    reservationATM.topUp(topUp);
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        int expectedBalance = initialBalance + (threads * topUpsPerThread * topUpAmount);
        assertEquals(expectedBalance, atmState.getBalance(Currency.RUB));
    }

    @Test
    void testConcurrentReserve() throws InterruptedException {
        int threads = 10;
        int reserveAmount = 100;
        int initialBalance = atmState.getBalance(Currency.RUB);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        Map<UUID, Map<Currency, Map<Nominal, Integer>>> reservations = Collections.synchronizedMap(new HashMap<>());

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                UUID reservationId = reservationATM.reserve(Currency.RUB, reserveAmount);
                reservations.put(reservationId, Map.of(Currency.RUB, Map.of(RubleNominal.RUB_100, 1)));
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        int expectedBalance = initialBalance - (threads * reserveAmount);
        assertEquals(expectedBalance, atmState.getBalance(Currency.RUB));
        assertEquals(threads, reservations.size());
        reservations.keySet().forEach(uuid -> {
            assertTrue(atmState.containsReservation(uuid));
        });
    }

    @Test
    void testConcurrentWithdrawReservation() throws InterruptedException {
        int threads = 10;
        int reserveAmount = 100;
        Map<UUID, Map<Currency, Map<Nominal, Integer>>> reservations = Collections.synchronizedMap(new HashMap<>());

        for (int i = 0; i < threads; i++) {
            UUID reservationId = reservationATM.reserve(Currency.RUB, reserveAmount);
            reservations.put(reservationId, Map.of(Currency.RUB, Map.of(RubleNominal.RUB_100, 1)));
        }

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger withdrawnAmount = new AtomicInteger(0);

        for (UUID reservationId : reservations.keySet()) {
            executor.submit(() -> {
                Map<Currency, Map<Nominal, Integer>> withdrawn = reservationATM.withdrawReservation(reservationId);
                withdrawnAmount.addAndGet(withdrawn.get(Currency.RUB).get(RubleNominal.RUB_100) * 100);
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(threads * reserveAmount, withdrawnAmount.get());
        reservations.keySet().forEach(uuid -> {
            assertThrows(IllegalStateException.class, () -> reservationATM.withdrawReservation(uuid));
        });
    }

    @Test
    void testConcurrentWithdrawAndReserve() throws InterruptedException {
        int withdrawCount = 5;
        int reserveCount = 5;
        int threadCount = withdrawCount + reserveCount;
        int withdrawalAmount = 100;
        int reserveAmount = 100;
        int initialBalance = atmState.getBalance(Currency.RUB);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Map<UUID, Map<Currency, Map<Nominal, Integer>>> reservations = Collections.synchronizedMap(new HashMap<>());


        for (int i = 0; i < threadCount; i++) {
            if (i % 2 == 0) {
                executor.submit(() -> {
                    reservationATM.withdraw(Currency.RUB, withdrawalAmount);
                    latch.countDown();
                });
            } else {
                executor.submit(() -> {
                    UUID reservationId = reservationATM.reserve(Currency.RUB, reserveAmount);
                    reservations.put(reservationId, Map.of(Currency.RUB, Map.of(RubleNominal.RUB_100, 1)));
                    latch.countDown();
                });
            }
        }

        latch.await();
        executor.shutdown();

        int totalWithdrawn = withdrawCount * withdrawalAmount;
        int totalReserved = reserveCount * reserveAmount;
        int expectedBalance = initialBalance - totalWithdrawn - totalReserved;

        assertEquals(expectedBalance, atmState.getBalance(Currency.RUB));
        assertEquals(reserveCount, reservations.size());
        reservations.keySet().forEach(uuid -> {
            assertTrue(atmState.containsReservation(uuid));
        });
    }
    
    @Test
    void testReserveExceedsMaxPercentage() {
        int initialBalance = atmState.getBalance(Currency.RUB);
        int maxAllowedReservation = getMaxAllowedReservation(initialBalance);
        int minNominal = atmState.getMinNominal(Currency.RUB).getNominal();
        maxAllowedReservation -= maxAllowedReservation % minNominal;

        // Attempt to reserve an amount slightly more than the allowed maximum
        int reserveAmount = maxAllowedReservation + minNominal;

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reservationATM.reserve(Currency.RUB, reserveAmount);
        });

        String expectedMessage = MessageFormat.format(Message.EXCEEDING_MAXIMUM_RESERVATION_AMOUNT.getPattern(), maxAllowedReservation);
        assertEquals(expectedMessage, exception.getMessage());
    }

    private static int getMaxAllowedReservation(int initialBalance) {
        double MAX_RESERVATION_PERCENTAGE;
        // Use reflection to get the actual MAX_RESERVATION_PERCENTAGE from Checker
        try {
            Field field = Checker.class.getDeclaredField("MAX_RESERVATION_PERCENTAGE");
            field.setAccessible(true);
            MAX_RESERVATION_PERCENTAGE = (double) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not access MAX_RESERVATION_PERCENTAGE from Checker via reflection", e);
        }
        // Calculate max allowed reservation amount as done in ReservationATM
        return (int) (initialBalance * (MAX_RESERVATION_PERCENTAGE / 100));
    }

}