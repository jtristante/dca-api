package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto;

public record MetadataDTO(
        String version,
        int status,
        String ticker,
        String interval,
        Boolean dividend
) {
}
