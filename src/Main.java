import atm.BaseATM;
import atm.MulticurrencyATM;
import atm.ReservationATM;
import currency.Currency;
import currency.EuroNominal;
import currency.Nominal;
import currency.RubleNominal;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        checkReservationAtm();
    }

    private static void checkReservationAtm() {
        Map<Nominal, Integer> rubleBanknotes = new HashMap<>();
        rubleBanknotes.put(RubleNominal.RUB_1000, 2);
        rubleBanknotes.put(RubleNominal.RUB_500, 1);
        rubleBanknotes.put(RubleNominal.RUB_50, 3);

        Map<Nominal, Integer> euroBanknotes = new HashMap<>();
        euroBanknotes.put(EuroNominal.EUR_500, 2);
        euroBanknotes.put(EuroNominal.EUR_100, 1);
        euroBanknotes.put(EuroNominal.EUR_20, 3);

        Map<Currency, Map<Nominal, Integer>> currencies = new HashMap<>();
        currencies.put(Currency.RUB, rubleBanknotes);
        currencies.put(Currency.EUR, euroBanknotes);

        rubleBanknotes.forEach((k, v) -> System.out.println(k.getNominal()));

        ReservationATM atm = new ReservationATM(currencies);

        System.out.println();
        System.out.println("ATM state before reservation: ");
        System.out.println("total amount RUB: " + atm.getBalance(Currency.RUB));
        System.out.println("min nominal RUB: " + atm.getMinNominal(Currency.RUB));
        System.out.println("total amount EUR: " + atm.getBalance(Currency.EUR));
        System.out.println("min nominal EUR: " + atm.getMinNominal(Currency.EUR));
        System.out.println();

        UUID reservationUuid = atm.reserve(Currency.RUB, 100);

        System.out.println("Reservation UUID: " + reservationUuid);
        System.out.println();

        Map<Currency, Map<Nominal, Integer>> withdrawnBanknotes = atm.withdrawReservation(reservationUuid);
        System.out.println("Withdrawn Banknotes: ");
        System.out.print("Currencies: ");
        withdrawnBanknotes.keySet().forEach(System.out::println);
        withdrawnBanknotes.get(Currency.RUB).forEach((k, v) -> System.out.println("Nominal: " + k + "; " + "Amount: " + v));
        System.out.println();


        reservationUuid = atm.reserve(Currency.EUR, 60);

        System.out.println("Reservation UUID: " + reservationUuid);
        System.out.println();

        withdrawnBanknotes = atm.withdrawReservation(reservationUuid);
        System.out.println("Withdrawn Banknotes: ");
        System.out.print("Currencies: ");
        withdrawnBanknotes.keySet().forEach(System.out::println);
        withdrawnBanknotes.get(Currency.EUR).forEach((k, v) -> System.out.println("Nominal: " + k + "; " + "Amount: " + v));

        System.out.println();
        System.out.println("ATM state after withdrawal: ");
        System.out.println("total amount RUB: " + atm.getBalance(Currency.RUB));
        System.out.println("total amount EUR: " + atm.getBalance(Currency.EUR));
        System.out.println("min nominal RUB: " + atm.getMinNominal(Currency.RUB));
        System.out.println("min nominal EUR: " + atm.getMinNominal(Currency.EUR));
    }

    private static void checkMulticurrencyAtm() {
        Map<Nominal, Integer> rubleBanknotes = new HashMap<>();
        rubleBanknotes.put(RubleNominal.RUB_1000, 2);
        rubleBanknotes.put(RubleNominal.RUB_500, 1);
        rubleBanknotes.put(RubleNominal.RUB_50, 3);

        Map<Nominal, Integer> euroBanknotes = new HashMap<>();
        euroBanknotes.put(EuroNominal.EUR_500, 2);
        euroBanknotes.put(EuroNominal.EUR_100, 1);
        euroBanknotes.put(EuroNominal.EUR_20, 3);

        Map<Currency, Map<Nominal, Integer>> currencies = new HashMap<>();
        currencies.put(Currency.RUB, rubleBanknotes);
        currencies.put(Currency.EUR, euroBanknotes);

        rubleBanknotes.forEach((k, v) -> System.out.println(k.getNominal()));

        MulticurrencyATM atm = new MulticurrencyATM(currencies);

        System.out.println();
        System.out.println("ATM state before withdrawal: ");
        System.out.println("total amount RUB: " + atm.getBalance(Currency.RUB));
        System.out.println("min nominal RUB: " + atm.getMinNominal(Currency.RUB));
        System.out.println("total amount EUR: " + atm.getBalance(Currency.EUR));
        System.out.println("min nominal EUR: " + atm.getMinNominal(Currency.EUR));
        System.out.println();

        Map<Currency, Map<Nominal, Integer>> withdrawnBanknotes = atm.withdraw(Currency.RUB, 100);
        System.out.println("Withdrawn Banknotes: ");
        System.out.print("Currencies: ");
        withdrawnBanknotes.keySet().forEach(System.out::println);
        withdrawnBanknotes.get(Currency.RUB).forEach((k, v) -> System.out.println("Nominal: " + k + "; " + "Amount: " + v));

        withdrawnBanknotes = atm.withdraw(Currency.EUR, 80);
        System.out.println();
        System.out.print("Currencies: ");
        withdrawnBanknotes.keySet().forEach(System.out::println);
        withdrawnBanknotes.get(Currency.EUR).forEach((k, v) -> System.out.println("Nominal: " + k + "; " + "Amount: " + v));

        System.out.println();
        System.out.println("ATM state after withdrawal: ");
        System.out.println("total amount RUB: " + atm.getBalance(Currency.RUB));
        System.out.println("total amount EUR: " + atm.getBalance(Currency.EUR));
        System.out.println("min nominal RUB: " + atm.getMinNominal(Currency.RUB));
        System.out.println("min nominal EUR: " + atm.getMinNominal(Currency.EUR));

        rubleBanknotes.clear();
        rubleBanknotes.put(RubleNominal.RUB_5000, 2);
        rubleBanknotes.put(RubleNominal.RUB_500, 4);
        rubleBanknotes.put(RubleNominal.RUB_100, 1);

        euroBanknotes.clear();
        euroBanknotes.put(EuroNominal.EUR_100, 1);
        euroBanknotes.put(EuroNominal.EUR_20, 2);

        atm.topUp(Map.of(Currency.RUB, rubleBanknotes, Currency.EUR, euroBanknotes));

        System.out.println();
        System.out.println("ATM state after top up: ");
        System.out.println("total amount RUB: " + atm.getBalance(Currency.RUB));
        System.out.println("total amount EUR: " + atm.getBalance(Currency.EUR));
        System.out.println("min nominal RUB: " + atm.getMinNominal(Currency.RUB));
        System.out.println("min nominal EUR: " + atm.getMinNominal(Currency.EUR));
    }

    private static void checkBaseAtm() {
        Map<RubleNominal, Integer> banknotes = new EnumMap<>(RubleNominal.class);
        banknotes.put(RubleNominal.RUB_5000, 2);
        banknotes.put(RubleNominal.RUB_1000, 2);
        banknotes.put(RubleNominal.RUB_500, 1);
        banknotes.put(RubleNominal.RUB_50, 3);

        banknotes.forEach((k, v) -> System.out.println(k.getNominal()));

        BaseATM atm = new BaseATM(banknotes);

        System.out.println("ATM state before withdrawal: ");
        System.out.println("total amount: " + atm.getBalance());
        System.out.println("min nominal: " + atm.getMinNominal());
        System.out.println();

        Map<RubleNominal, Integer> withdrawnBanknotes = atm.withdraw(500);
        System.out.println("Withdrawn Banknotes: ");
        withdrawnBanknotes.forEach((k, v) -> System.out.println(k + " " + v));

        System.out.println();
        System.out.println("ATM state after withdrawal: ");
        System.out.println("total amount: " + atm.getBalance());
        System.out.println("min nominal: " + atm.getMinNominal());

        banknotes = new EnumMap<>(RubleNominal.class);
        banknotes.put(RubleNominal.RUB_5000, 2);
        banknotes.put(RubleNominal.RUB_500, 4);
        banknotes.put(RubleNominal.RUB_100, 3);

        atm.topUp(banknotes);

        System.out.println();
        System.out.println("ATM state after top up: ");
        System.out.println("total amount: " + atm.getBalance());
        System.out.println("min nominal: " + atm.getMinNominal());
    }
}