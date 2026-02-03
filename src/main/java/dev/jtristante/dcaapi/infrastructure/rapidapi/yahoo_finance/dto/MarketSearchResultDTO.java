package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto;

public record MarketSearchResultDTO(
        String shortname,
        String quoteType,
        String symbol,
        String index,
        Double score,
        String typeDisp,
        String longname,
        String exchDisp,
        String sector,
        String sectorDisp,
        String industry,
        String industryDisp
) {
}
