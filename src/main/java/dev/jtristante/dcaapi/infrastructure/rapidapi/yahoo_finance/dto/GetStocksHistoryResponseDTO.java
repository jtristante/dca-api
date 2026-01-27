package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto;

import java.util.List;

public record
GetStocksHistoryResponseDTO(
        MetadataDTO meta,
        List<StockHistoryDTO> body
) {
}
