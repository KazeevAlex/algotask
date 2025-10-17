package com.example.algotask.message;

public enum Message {
    INSUFFICIENT_FUNDS("Недостаточно средств в банкомате. Повторите попытку позже."),
    AMOUNT_MUST_BE_MULTIPLE_OF("Сумма должна быть кратна {0}"),
    NEAREST_AVAILABLE_AMOUNTS("Запрошенная сумма недоступна. Ближайшие доступные суммы для снятия: {0} и {1}"),
    RESERVATION_NOT_EXIST("Отсутствует резервирование с кодом: {0}");

    private final String pattern;

    Message(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
