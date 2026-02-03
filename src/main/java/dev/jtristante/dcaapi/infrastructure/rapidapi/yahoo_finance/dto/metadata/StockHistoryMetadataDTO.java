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
}
