import java.util.EnumMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        checkBaseAtm();
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