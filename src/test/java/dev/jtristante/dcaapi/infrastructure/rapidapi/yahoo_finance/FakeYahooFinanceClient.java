package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance;

import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.api.YahooFinanceApi;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.GetStocksHistoryResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.IntervalType;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResultDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.StockHistoryDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.metadata.MarketSearchMetadataDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.metadata.StockHistoryMetadataDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("test")
public class FakeYahooFinanceClient implements YahooFinanceApi {

    @Override
    public GetStocksHistoryResponseDTO getStocksHistory(@NotNull String symbol, @NotNull IntervalType interval, Integer limit, Boolean dividend) {
        return new GetStocksHistoryResponseDTO(
                new StockHistoryMetadataDTO("v2.0", 200, symbol, interval.getCode(), dividend),
                List.of(
                        new StockHistoryDTO("2026-01-12", 1768194000, 259.16, 261.82, 254.93, 255.53, 48509020L),
                        new StockHistoryDTO("2026-01-19", 1768798800, 252.73, 254.79, 243.42, 248.04, 54076600L),
                        new StockHistoryDTO("2026-01-26", 1769403600, 251.48, 261.95, 249.8, 259.48, 61320340L),
                        new StockHistoryDTO("2026-02-02", 1770008400, 260.03, 278.95, 259.205, 276.49, 76239815L)
                )
        );
    }

    @Override
    public MarketSearchResponseDTO searchMarket(@NotNull String search) {
        String searchLower = search.toLowerCase();

        // Return specific test data based on search term
        if (searchLower.contains("noname") || searchLower.contains("1open")) {
            // Test case: symbol with no name should be filtered out
            return new MarketSearchResponseDTO(
                    new MarketSearchMetadataDTO("v1.0", 200, "https://steadyapi.com", search, "2026-02-03T00:00:00Z"),
                    List.of(
                            new MarketSearchResultDTO(
                                    null,
                                    "EQUITY",
                                    "1OPEN.MI",
                                    "quotes",
                                    100.0,
                                    "Equity",
                                    null,
                                    "MI",
                                    null,
                                    null,
                                    null,
                                    null
                            )
                    )
            );
        } else if (searchLower.contains("unsupported") || searchLower.contains("vanguard") || searchLower.contains("gold")) {
            // Test case: unsupported quote types should be filtered out
            return new MarketSearchResponseDTO(
                    new MarketSearchMetadataDTO("v1.0", 200, "https://steadyapi.com", search, "2026-02-03T00:00:00Z"),
                    List.of(
                            new MarketSearchResultDTO(
                                    "Vanguard Total Stock Market Index Fund",
                                    "MUTUALFUND",
                                    "VTSMX",
                                    "quotes",
                                    5000.0,
                                    "Mutual Fund",
                                    "Vanguard Total Stock Market Index Fund Admiral Shares",
                                    "NASDAQ",
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new MarketSearchResultDTO(
                                    "Gold Futures",
                                    "FUTURE",
                                    "GC=F",
                                    "quotes",
                                    8000.0,
                                    "Futures",
                                    "Gold Futures,Apr-2026",
                                    "COMEX",
                                    null,
                                    null,
                                    null,
                                    null
                            )
                    )
            );
        } else if (searchLower.contains("test") || searchLower.contains("msft")) {
            // Test case: empty DB flow - returns valid symbols to save
            return new MarketSearchResponseDTO(
                    new MarketSearchMetadataDTO("v1.0", 200, "https://steadyapi.com", search, "2026-02-03T00:00:00Z"),
                    List.of(
                            new MarketSearchResultDTO(
                                    "Bitcoin USD",
                                    "CRYPTOCURRENCY",
                                    "TEST-BTC-USD",
                                    "quotes",
                                    43153.6,
                                    "Cryptocurrency",
                                    "Bitcoin USD",
                                    "CCC",
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new MarketSearchResultDTO(
                                    "Microsoft Corp",
                                    "EQUITY",
                                    "TEST-MSFT",
                                    "quotes",
                                    20076.0,
                                    "Equity",
                                    "Microsoft Corporation",
                                    "NASDAQ",
                                    "Technology",
                                    "Technology",
                                    "Software",
                                    "Software"
                            )
                    )
            );
        } else {
            // Default: return empty list for any other search
            return new MarketSearchResponseDTO(
                    new MarketSearchMetadataDTO("v1.0", 200, "https://steadyapi.com", search, "2026-02-03T00:00:00Z"),
                    List.of()
            );
        }
    }
}
