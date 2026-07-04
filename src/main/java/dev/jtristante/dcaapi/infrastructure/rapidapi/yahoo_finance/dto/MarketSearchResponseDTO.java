package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto;

import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.metadata.MarketSearchMetadataDTO;

import java.util.List;

public record MarketSearchResponseDTO(
        MarketSearchMetadataDTO meta,
        List<MarketSearchResultDTO> body
) {
}
