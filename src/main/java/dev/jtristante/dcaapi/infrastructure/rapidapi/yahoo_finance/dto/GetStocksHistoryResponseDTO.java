package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto;

import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.metadata.StockHistoryMetadataDTO;

import java.util.List;

public record
GetStocksHistoryResponseDTO(
        StockHistoryMetadataDTO meta,
        List<StockHistoryDTO> body
) {
}
