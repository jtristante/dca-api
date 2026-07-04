package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.metadata;

public class MarketSearchMetadataDTO extends MetadataDTO {

    private final String copywrite;
    private final String symbol;
    private final String processedTime;

    public MarketSearchMetadataDTO(String version, int status, String copywrite, String symbol, String processedTime) {
        super(version, status);
        this.copywrite = copywrite;
        this.symbol = symbol;
        this.processedTime = processedTime;
    }

    public String getCopywrite() {
        return copywrite;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getProcessedTime() {
        return processedTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String version;
        private int status;
        private String copywrite;
        private String symbol;
        private String processedTime;

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder copywrite(String copywrite) {
            this.copywrite = copywrite;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder processedTime(String processedTime) {
            this.processedTime = processedTime;
            return this;
        }

        public MarketSearchMetadataDTO build() {
            return new MarketSearchMetadataDTO(version, status, copywrite, symbol, processedTime);
        }
    }
}
