package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto;

public enum IntervalType {

    M1("1m", "1 minute"),
    M2("2m", "2 minutes"),
    M3("3m", "3 minutes"),
    M4("4m", "4 minutes"),
    M5("5m", "5 minutes"),
    M15("15m", "15 minutes"),
    M30("30m", "30 minutes"),
    H1("1h", "1 hour"),
    D1("1d", "1 day"),
    W1("1wk", "1 week"),
    MO1("1mo", "1 month"),
    Q1("1qty", "1 quarter");

    private final String code;
    private final String description;

    IntervalType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static IntervalType fromCode(String code) {
        for (IntervalType tf : values()) {
            if (tf.code.equalsIgnoreCase(code)) {
                return tf;
            }
        }
        throw new IllegalArgumentException("Invalid timeframe code: " + code);
    }
}