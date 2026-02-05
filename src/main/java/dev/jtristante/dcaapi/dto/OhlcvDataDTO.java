package dev.jtristante.dcaapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OhlcvDataDTO(
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Long volume,
        BigDecimal dividend
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDate date;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private Long volume;
        private BigDecimal dividend;

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder open(BigDecimal open) {
            this.open = open;
            return this;
        }

        public Builder high(BigDecimal high) {
            this.high = high;
            return this;
        }

        public Builder low(BigDecimal low) {
            this.low = low;
            return this;
        }

        public Builder close(BigDecimal close) {
            this.close = close;
            return this;
        }

        public Builder volume(Long volume) {
            this.volume = volume;
            return this;
        }

        public Builder dividend(BigDecimal dividend) {
            this.dividend = dividend;
            return this;
        }

        public OhlcvDataDTO build() {
            return new OhlcvDataDTO(date, open, high, low, close, volume, dividend);
        }
    }
}
