package com.example.algotask.atm;

import com.example.algotask.currency.Currency;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ExpiredReservationScheduler {
    private static final long RESERVATION_EXPIRATION_TIME_MINUTES = 120;

    private final AtmState atmState;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final Map<UUID, ScheduledFuture<?>> scheduledExpirations = new ConcurrentHashMap<>();

    public ExpiredReservationScheduler(AtmState atmState) {
        this.atmState = atmState;
    }

    public void schedule(UUID reservationUuid, Currency currency) {
        Runnable task = getExpirationTask(reservationUuid, currency);
        var scheduledExpiration = scheduler.schedule(task, RESERVATION_EXPIRATION_TIME_MINUTES, TimeUnit.MINUTES);
        scheduledExpirations.put(reservationUuid, scheduledExpiration);
    }

    public void cancel(UUID reservationUuid) {
        var scheduledExpiration = remove(reservationUuid);
        if (scheduledExpiration != null) {
            scheduledExpiration.cancel(false);
        }
    }

    private Runnable getExpirationTask(UUID reservationUuid, Currency currency) {
        return () -> {
            if (remove(reservationUuid) == null) {
                return;
            }
            var expiredReservation = atmState.removeReservation(reservationUuid);
            if (expiredReservation == null || expiredReservation.isEmpty()) {
                return;
            }

            currency.getLock().lock();
            try {
                atmState.put(expiredReservation);
            } finally {
                currency.getLock().unlock();
            }
        };
    }

    private ScheduledFuture<?> remove(UUID reservationUuid) {
        return scheduledExpirations.remove(reservationUuid);
    }
}
