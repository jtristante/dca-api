package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.metadata;

public class StockHistoryMetadataDTO extends MetadataDTO {

    private final String ticker;
    private final String interval;
    private final Boolean dividend;

    public StockHistoryMetadataDTO(String version, int status, String ticker, String interval, Boolean dividend) {
        super(version, status);
        this.ticker = ticker;
        this.interval = interval;
        this.dividend = dividend;
    }

    public String getTicker() {
        return ticker;
    }

    public String getInterval() {
        return interval;
    }

    public Boolean getDividend() {
        return dividend;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String version;
        private int status;
        private String ticker;
        private String interval;
        private Boolean dividend;

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder ticker(String ticker) {
            this.ticker = ticker;
            return this;
        }

        public Builder interval(String interval) {
            this.interval = interval;
            return this;
        }

        public Builder dividend(Boolean dividend) {
            this.dividend = dividend;
            return this;
        }

        public StockHistoryMetadataDTO build() {
            return new StockHistoryMetadataDTO(version, status, ticker, interval, dividend);
        }
    }
}
