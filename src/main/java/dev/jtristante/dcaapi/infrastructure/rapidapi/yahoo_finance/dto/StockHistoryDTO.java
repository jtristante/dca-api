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
}
