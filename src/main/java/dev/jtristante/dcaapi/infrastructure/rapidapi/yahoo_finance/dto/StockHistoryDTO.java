package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StockHistoryDTO(
        String timestamp,
        @JsonProperty("timestamp_unix") long timestampUnix,
        Double open,
        Double high,
        Double low,
        Double close,
        Long volume
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String timestamp;
        private long timestampUnix;
        private Double open;
        private Double high;
        private Double low;
        private Double close;
        private Long volume;

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder timestampUnix(long timestampUnix) {
            this.timestampUnix = timestampUnix;
            return this;
        }

        public Builder open(Double open) {
            this.open = open;
            return this;
        }

        public Builder high(Double high) {
            this.high = high;
            return this;
        }

        public Builder low(Double low) {
            this.low = low;
            return this;
        }

        public Builder close(Double close) {
            this.close = close;
            return this;
        }

        public Builder volume(Long volume) {
            this.volume = volume;
            return this;
        }

        public StockHistoryDTO build() {
            return new StockHistoryDTO(
                    timestamp,
                    timestampUnix,
                    open,
                    high,
                    low,
                    close,
                    volume
            );
        }
    }
}
