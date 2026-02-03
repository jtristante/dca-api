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
}
