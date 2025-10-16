package atm;

import currency.Nominal;
import message.Message;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class BaseATM {

    private final BaseAtmState atmState;

    public BaseATM(BaseAtmState atmState) {
        this.atmState = atmState;
    }

    public Map<Nominal, Integer> withdraw(Integer withdrawalAmount) {
        checkWithdrawalAmount(withdrawalAmount);
        var withdrawalBanknotes = prepareWithdrawalBanknotes(withdrawalAmount);
        removeWithdrawalBanknotes(withdrawalBanknotes);
        return withdrawalBanknotes;
    }

    public void topUp(Map<Nominal, Integer> topUpBanknotes) {
        atmState.put(topUpBanknotes);
    }

    private void removeWithdrawalBanknotes(Map<Nominal, Integer> withdrawalBanknotes) {
        atmState.removeAll(withdrawalBanknotes);
    }

    private Map<Nominal, Integer> prepareWithdrawalBanknotes(Integer withdrawalAmount) {
        Map<Nominal, Integer> withdrawalBanknotes = new HashMap<>();
        int remainsAmount = withdrawalAmount;

        for (var entry : atmState.getBanknotes().entrySet()) {
            Nominal key = entry.getKey();
            Integer atmBanknoteAmount = entry.getValue();
            int nominal = key.getNominal();

            if (nominal > remainsAmount) {
                continue;
            }

            int requiredBanknoteAmount = remainsAmount / nominal;
            if (atmBanknoteAmount < requiredBanknoteAmount) {
                requiredBanknoteAmount = atmBanknoteAmount;
            }

            remainsAmount -= nominal * requiredBanknoteAmount;
            withdrawalBanknotes.put(key, requiredBanknoteAmount);

            if (remainsAmount == 0) {
                break;
            }
        }

        checkRemainsAmount(withdrawalAmount, remainsAmount);

        return withdrawalBanknotes;
    }

    private void checkRemainsAmount(Integer withdrawalAmount, Integer remainsAmount) {
        if (remainsAmount == 0) {
            return;
        }
        int nearestMin = withdrawalAmount - remainsAmount;
        int nearestMax = atmState.getBanknotes().keySet().stream()
                .mapToInt(Nominal::getNominal)
                .filter(i -> i > withdrawalAmount)
                .min()
                .getAsInt();

        String message = MessageFormat.format(Message.NEAREST_AVAILABLE_AMOUNTS.getPattern(), nearestMin, nearestMax);
        throw new IllegalStateException(message);
    }

    private void checkWithdrawalAmount(Integer withdrawalAmount) {
        if (withdrawalAmount > atmState.getBalance()) {
            throw new IllegalStateException(Message.INSUFFICIENT_FUNDS.getPattern());
        }
        Integer minNominalValue = atmState.getMinNominal().getNominal();
        if (withdrawalAmount % minNominalValue > 0) {
            String message = MessageFormat.format(Message.AMOUNT_MUST_BE_MULTIPLE_OF.getPattern(), minNominalValue);
            throw new IllegalStateException(message);
        }
    }
}
