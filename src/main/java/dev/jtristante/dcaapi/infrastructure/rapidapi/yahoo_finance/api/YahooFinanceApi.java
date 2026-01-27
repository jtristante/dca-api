package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.api;

import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.GetStocksHistoryResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.IntervalType;
import jakarta.validation.constraints.NotNull;

public interface YahooFinanceApi {
    GetStocksHistoryResponseDTO getStocksHistory(@NotNull String symbol, @NotNull IntervalType interval, Integer limit, Boolean dividend);
}
